docs/ERP_v0_Integration_Plan_ShadowMode_v1.0.md
# ERP v0 통합 계획(Shadow Mode 포함) v1.0
작성일: 2025-08-27  
대상: MES 코어(지시→전이→실적)와 ‘최소 트러블’ ERP v0 연동

본 문서는 “최소 트러블 · 포트폴리오 임팩트 극대화”를 목표로 확정된 ERP v0 구현 기준을 정리한다. 추가 아이디어는 Shadow Mode(그림자 전송)만 채택한다. 이 문서의 값/정책은 ‘확정’ 기준이며, 변경 시 버전업한다.

---

## 1) 스코프(확정)

- ERP → MES
  - 생산계획 Create/Update
  - 기준정보 증분: 품목(Item)/단위(UoM)/BOM(헤더·라인)

- MES → ERP
  - 작업지시 Create, 상태 전이(P→R→C)
  - 실적 Create
  - 자재소모 Backflush(표준 소모) — Actual은 v1로 이월

- 공통 비기능
  - 시간: UTC ISO(…Z) only
  - 상태맵: P/R/C 동형, 역전 금지(P→R→C only)
  - 멱등: Idempotency-Key 필수(양방향)
  - 신뢰성: Outbox/Inbox + 지수 백오프
  - 에러: { code, message, path, method, timestamp } 표준

---

## 2) Shadow Mode(그림자 전송) 설계(확정)

- 목적: 실제 ERP 반영 없이 “전송·검증·로깅”만 수행. 데모·리허설·장애 분석에 활용
- 동작
  - 플래그: ERP_SHADOW_MODE=true 일 때 활성
  - 처리: 요청 payload 서명/검증 → ERP 호출을 ‘모의’로 대체(미전송) → 요청/응답(모의) 전량 로깅
  - 로그: endpoint, payload hash, validation result, wouldSend=true, timestamp
  - UI: Outbox 대시보드에서 Shadow 아이콘/필터 제공

- 보안·프라이버시
  - 민감 필드는 마스킹(키/토큰/개인식별자 등)

- 종료 전략
  - Shadow OFF(=실전 전송)로 전환 시 Outbox 단위로 토글(엔드포인트별 가능)

---

## 3) 데이터 계약(확정)

- 식별자/키
  - workOrderId(UUID, 참조 키), workOrderNumber(표시용)
  - planId(+planLineNo) → workOrder 매핑

- 시간
  - 요청/응답 모두 UTC ISO(…Z) only, OffsetDateTime.parse 고정

- 상태맵
  - P/R/C 동형, 역전 금지(P→R→C only)

- 기준선(finalBaseline)
  - 정의: max(wo.startTs ?? wo.createdAt, 해당 WO의 마지막 실적 종료시각)
  - 제공: /work-orders/{id} 상세에 finalBaseline(UTC ISO) 필드 추가(확정)

- UoM/정밀도
  - UoM 매핑(map_uom) + factor
  - 숫자 스케일 DECIMAL(18,6), 반올림 Half up

### 엔드포인트(초안 스키마)

- ERP → MES
  - POST /mes/plans
  - PUT  /mes/plans/{planId}
  - GET  /mes/items?updatedSince=…
  - GET  /mes/boms?updatedSince=…

- MES → ERP
  - POST /erp/work-orders
  - PUT  /erp/work-orders/{workOrderId}/status
  - POST /erp/performances
  - POST /erp/consumptions/backflush

에러 포맷 예)
    { "code":"DUPLICATE_KEY", "message":"DUPLICATE_KEY", "path":"/erp/performances", "method":"POST", "timestamp":"2025-08-27T01:23:45Z" }

---

## 4) 멱등/재시도/감사(확정)

- 멱등
  - Idempotency-Key 헤더 필수(양방향)
  - TTL=72h(동일 키 재요청 → 200/201 with existing)

- Outbox(발신)
  - 테이블: mes_outbox(outbox_id, event_type, payload, status, retry_count, next_retry_at, last_error, created_at)
  - 재시도: 1m→5m→15m→1h→6h(최대 5회) 후 FAILED 고정

- Inbox(수신)
  - 테이블: mes_inbox(inbox_id, idempotency_key UNIQUE, endpoint, payload, status, created_at)
  - 동일 키 수신 시 중복 처리 방지

- 대시보드
  - Outbox 상태(SENT/RETRY/FAILED), 최근 에러 메시지, 재전송 버튼, Shadow 필터

---

## 5) API 제한/성능/보안(확정)

- 제한
  - 기간 윈도: 최대 31일
  - 페이지 상한: 100
  - 정렬 기본: startTime,desc

- 보안
  - 인증: v0=API Key, v1=Bearer(만료/로테이션) 예정
  - 서비스 계정 분리, IP Allowlist, 레이트 리밋(기본 60 req/min), 타임아웃 5s
  - 시크릿: ERP_BASE_URL, ERP_API_KEY(or Bearer), DB, CORS Origin — 전부 env

---

## 6) DB/인덱스(확정)

- 인덱스
  - 실적: (work_order_id, start_time), (equipment_id, start_time)
  - 지시: (status_code_id), (equipment_id, start_ts)

- Outbox/Inbox
  - Outbox: 상태/재시도 시각/오류 컬럼 필수
  - Inbox: idempotency_key UNIQUE

---

## 7) 테스트/대사(확정)

- 스모크(자동)
  - 현 6케이스 유지(viewer 403, op 201, DUPLICATE_KEY, TIME_ORDER_INVALID, WO_STATUS_INVALID, PERF_BEFORE_WO)
  - 왕복 2케이스 추가(지시/실적 ERP 전송, Shadow ON 포함)

- 컨트랙트 테스트
  - 스키마/UTC/멱등/에러 포맷 자동 검증

- 대사(Reconciliation)
  - 일일 합계(지시/실적/소모) 비교 리포트(스케줄 02:00 UTC), 불일치 알림

---

## 8) 1주 실행 계획(확정)

- Day 1
  - OpenAPI v0(6엔드포인트) 초안 freeze
  - Outbox/Inbox 테이블, 공통 멱등/에러 미들웨어

- Day 2
  - ERP→MES: 생산계획(POST/PUT), 기준정보 증분(GET) 구현
  - 컨트랙트 테스트 1차

- Day 3
  - MES→ERP: 지시 Create, 상태 전이(P→R→C)
  - Shadow Mode ON(모의 전송/로깅) 시연

- Day 4
  - MES→ERP: 실적 Create, Backflush
  - Outbox 재시도 시나리오(타임아웃/500/409) 검증

- Day 5
  - Outbox 대시보드(상태/에러/재전송/Shadow 필터)
  - 대사 SQL/리포트, 알림 연동
  - 문서/데모/롤백 플랜 고정

---

## 9) DoD(완료 기준)

- 왕복 6엔드포인트 정상 동작(UTC/상태맵/멱등 준수)
- Outbox/Inbox로 재시도·감사 가능(Shadow Mode 포함)
- 컨트랙트/E2E/스모크 통과(추가 왕복 2케이스 포함)
- 상세(/work-orders/{id})에 finalBaseline 제공
- 대사 리포트 일일 실행(합계 불일치 알림)

---

## 10) 변경 관리/버전

- 본 문서의 정책/값 변경 시 파일명 v1.x+1로 업데이트
- OpenAPI 스펙은 tag=v0로 freeze, 파라미터/필드 추가 시 마이너, 제거·의미 변경 시 메이저

---

## [부록] 예시 페이로드

작업지시(MES→ERP)
    {
      "workOrderId": "WO-uuid",
      "workOrderNumber": "WO-2025-0001",
      "itemId": "I-0001",
      "qty": 500,
      "status": "P",
      "startTs": "2025-08-20T06:00:00Z",
      "idempotencyKey": "uuid"
    }

실적(MES→ERP)
    {
      "workOrderId": "WO-uuid",
      "itemId": "I-0001",
      "processId": "P-0001",
      "equipmentId": "E-0001",
      "goodQty": 98.0,
      "defectQty": 2.0,
      "startTime": "2025-08-20T07:00:00Z",
      "endTime": "2025-08-20T07:30:00Z",
      "idempotencyKey": "uuid"
    }

Backflush(MES→ERP)
    {
      "workOrderId": "WO-uuid",
      "lines": [
        { "componentId": "RM-001", "qty": 98.0, "uom": "EA" },
        { "componentId": "RM-002", "qty": 49.0, "uom": "EA" }
      ],
      "idempotencyKey": "uuid"
    }