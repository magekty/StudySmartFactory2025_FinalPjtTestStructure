# M0. 계약·환경 락(반나절)
- 산출물
OpenAPI v0(6종): 계획/품목·UoM·BOM 증분, 지시 생성/전이, 실적, Backflush
공통정책 문구: UTC ISO, P/R/C, Idempotency 헤더, 기간 31일/페이지 100, UoM 18,6 half-up
플래그: Shadow 전역 ON, 엔드포인트별(지시·실적 OFF 후보 / Backflush·Cost ON)
- DoD
스펙 파일 freeze, 환경키(ERP_BASE_URL, ERP_API_KEY, Shadow 토글) 배치
- Gate
스펙 변경 No. 모든 팀 합의 OK이면 M1 진입

# M1. ERP BE(Java) 스켈레톤(1일)
- 산출물
Spring Boot 서비스: API Key 필터, Idempotency 인터셉터, 표준 에러 핸들러
6 엔드포인트 Stub + Swagger UI
- DoD
모든 엔드포인트 200/201/202 기본 응답, 헤더 기반 멱등 재생 확인
- Gate
Swagger 열람 OK, 헤더 멱등(POST 2회 동일 바디) 확인되면 M2

# M2. ERP→MES 증분 제공(계획·BOM·품목)(1일)
- 산출물
GET /items?updatedSince, GET /boms?updatedSince, POST/PUT /plans
BOM 헤더/라인·UoM 필드/유효기간·삭제정책 반영
- DoD
updatedSince 증분·페이지 상한·UTC 검증 통과, 샘플 데이터로 Backflush 산식 검증 OK
- Gate
MES 캐시 갱신 테스트(샘플) 통과 시 M3
# test 목록
- 보안 가드(최상단)
어떻게: Postman/.http로 헤더 없이 호출 → X-API-Key 넣고 다시 호출
기대값: 헤더 없음=401, 헤더 있음=2xx
실패 시: ApiKey 필터 화이트리스트 경로 확인(/swagger-ui/, /v3/api-docs/), SecurityContext 주입(ROLE_API) 확인
- 엔드포인트 기본 응답(ERP)
어떻게: POST /erp/work-orders → PUT /erp/work-orders/{id}/status → POST /erp/performances → POST /erp/consumptions/backflush
기대값: 201, 200, 201, 202(backflush)
실패 시: DTO 필수 필드/UTC(…Z) 여부, 상태맵(P/R/C) 위반 확인
- 멱등 재생(hit) 확인
어떻게: 같은 X-Idempotency-Key로 POST /erp/performances 두 번 연속
기대값: 1·2회 응답 body의 식별자(performanceId)가 동일
실패 시: 헤더 위치 재확인(본문 idempotencyKey는 무시), 앱 재시작으로 캐시 날린 건 아닌지 체크
- Swagger 사용 룰(개발/운영)
어떻게: dev=키 비워 열람, stg/prod=Authorize에서 X-API-Key 넣고 Try it out
기대값: 화면은 열리고, 호출은 Authorize 이후 2xx
실패 시: /swagger-ui/, /v3/api-docs/ 화이트리스트/permitAll 경로 재확인
- Outbox 워커 → ERP 호출(MES→ERP)
어떻게: mes_outbox에 PENDING 이벤트 적재(지시/전이/실적/백플러시)
기대값:
Live(지시/전이/실적)=SENT(2xx)
Shadow(Backflush/Cost)=SHADOWED(전송 안 함)
실패 시: ERP_BASE_URL/API_KEY 오타, Shadow 플래그, idempotencyKey 생성 여부, 백오프(nextRetryAt) 계산 확인
- ERP→MES 증분(Items/BOM/Plans)
어떻게:
GET /items?updatedSince=… → 전체→증분
GET /boms?updatedSince=… → 헤더/라인+유효기간
POST/PUT /plans → isDeleted tombstone 포함
기대값: updatedSince 기준으로만 내려오고, isDeleted=true 케이스 포함
실패 시: OffsetDateTime 파싱(UTC), 페이지 상한 100 준수, 샘플 시드 확인
# 아래 미완료
- MES 캐시 동기화 스케줄러(5분)
어떻게: ERP에서 샘플 수정/삭제 후 5분 대기 → MES 캐시(tb_item/tb_bom_header/line/plan_line) 반영 확인
기대값: 신규/수정/삭제(tombstone) 3종 반영
실패 시: updatedSince 기준 시각 불일치(UTC↔KST 혼동) 유틸 고정
- Shadow 운용(24h 관찰 대상)
어떻게: Backflush/Cost 전송 → ERP 202 응답, Outbox는 SHADOWED 누적
기대값: 대사 리포트에 Shadow 건수/합계 잡힘
실패 시: Shadow 전역/엔드포인트 플래그 값 재확인
- 대사 리포트(02:00 UTC)
어떻게: 일자 기준 지시/실적/소모 합계 비교 리포트 실행
기대값: 불일치 0(또는 허용 범위), 실패 시 알림 수신
실패 시: 집계 윈도우(KST→UTC 변환) 경계, 인덱스 점검
- 표준 에러 포맷/시간 정책 최종 점검
어떻게: 의도적 오류 호출(TIME_ORDER_INVALID, WO_STATUS_INVALID 등)
기대값: {code, message, path, method, timestamp} 일관 / 요청·응답은 UTC ISO(…Z)
실패 시: 예외 핸들러 바인딩/메시지 코드 매핑 재확인
- 포트/프로필/로그(운영성)
어떻게: MES 8080 / ERP 8081 고정, dev/stg/prod 분리, 기동 로그에서 포트 라인 확인
기대값: 디버그 임의 포트 없음, 키·URL·프로필 정확
실패 시: launchSettings/application.yml 프로필·포트 값 재검토
- WPF 콘솔 연결(라이트)
어떻게: Base URL=http://localhost:8081 + API Key 입력 → 지시/전이/실적 폼 테스트
기대값: 요청→응답 OK, Shadow 배지 표식
실패 시: NSwag 클라이언트 생성 시 스키마/경로 확인

# M3. MES→ERP 라이브 후보(지시/전이/실적) 연결(1일)
- 산출물
ERP BE 실제 처리(지시 생성·전이, 실적 저장) + 멱등 hit 재생 표식 헤더(X-Idempotency-Replayed)
MES Outbox 워커 → ERP 호출(지시/전이/실적만 Shadow OFF)
- DoD
지시/전이/실적 왕복 스모크 ALL PASS(멱등 포함), 에러 포맷·타임아웃·재시도 정상
- Gate
Outbox RETRY/FAILED 0, validation fail <0.5%면 M4

# 진행 계획
목표: ERP /plans(POST/PUT, isDeleted tombstone) → MES 계획 캐시 upsert + 증분 스케줄 연결
작업 순서
커서 키 추가: tb_sync_cursor에 erp_plans 등록(초기 1970-01-01).
MES IncrementalSyncService.syncPlans() 추가
upsert 키=(plan_id, plan_line_no)
tombstone=is_deleted=1, deleted_at=UTC_TIMESTAMP()
updatedAt 기반 커서 갱신(없으면 수신 시각으로 대체)
스케줄러에 plans 동기 포함(5분 주기)
스모크 3케이스
신규: POST /plans → 캐시 201/업서트 반영
수정: PUT /plans → 필드 변경 반영
삭제: PUT isDeleted=true → 캐시 tombstone 반영
DoD
계획 캐시에서 신규/수정/삭제 3종 반영
커서 erp_plans 최신화
에러 발생 시 표준 400 포맷으로 로깅
오늘 마무리 리스크 메모(재발 방지)

# M4. Backflush·Cost Shadow 운용 고정(반나절)
- 산출물
Backflush: ERP 수신/검증만(Shadow ON), 에러 코드(BOM/UoM/유효기간) 정리
Cost: Shadow 전용 payload 로깅(EstCost는 MES 화면만)
- DoD
Backflush 202 + wouldSend 로깅 누적, 대사 리포트에 Shadow 통계 포함
- Gate
Shadow 로깅/대사 지표 확인되면 M5

### 목차

- M5. 접착-1 Plan → Work Order 발행
- M6. 접착-2 Performance → Backflush 생성(Shadow) + Cost wouldSend
- M7. ERP FE(C#) 1차: 대시보드·Outbox 리스트·폼(지시/전이/실적)
- M8. ERP FE(C#) 2차: BOM 뷰어/리비전/유효기간
- M9. 원가 기초: item_cost/uom_conv + /cost/simulate·/cost/post(API)
- M10. ERP DB 영속화 전환(메모리→DB)
- 부록: 표준 DoD/리스크/체크리스트

## M5. 접착-1 Plan → Work Order 발행 (수동/배치)
- 목적

계획 라인을 실제 생산 흐름으로 연결(PlanLine → WO 생성). 실적·소모가 이어지도록 1:1 브리지 구성.
- 스코프

수동 발행 API + 배치(옵션) 중 택1 또는 병행
중복 발행 방지(매핑 키 고정)
- 산출물

- API(내부): POST /internal/plans/{planId}/{lineNo}/issue-wo
매핑: plan_line → work_order 1:1 기록(issued_wo_id 또는 매핑 테이블)
- 로그: 발행 이력(누가/언제/몇 개)
# DB 변경(선택지 중 택1)

- 옵션 A: 컬럼 추가
tb_production_plan_line.issued_wo_id VARCHAR(36) NULL
### 옵션 B: 매핑 테이블
tb_plan_wo_map(plan_id, plan_line_no, work_order_id, created_at)

- API 계약(내부)

POST /internal/plans/{planId}/{lineNo}/issue-wo
요청: { "force": false }
응답: { "workOrderId": "WO-...", "status": "P", "issued": true }
규칙: 이미 issued_wo_id가 있으면 409(CONFLICT) 또는 force=true 시 재발행
- 비즈 규칙

WO 수량 = plan_line.qty, status=P
키 충돌 방지: uk_wo_number, work_order_id UUID
발행 후 plan_line.issued_wo_id 세팅(또는 매핑 테이블 INSERT)
- DoD

같은 라인 재발행 방지
발행된 WO가 목록/상태 전이 흐름에서 사용 가능
- 테스트

신규 라인 → 발행 201 → WO 조회 OK
재발행 시도 → 409 또는 force=true로 재발행 허용(정책 선택)
- 리스크/완화

중복 발행: issued_wo_id NOT NULL 조건/유니크 보조
FK 실패: item_id/프로세스/설비 키 사전 검증
## M6. 접착-2 Performance → Backflush(Shadow) + Cost wouldSend
- 목적

실적 발생 시 BOM 기준 소모량을 산출하고 Backflush 이벤트를 자동 생성(Shadow 전송). 동시에 Cost wouldSend를 남겨 원가 기초를 축적.
- 스코프

Perf 저장 직후 훅(Hook)에서 Outbox 적재 2건
BACKFLUSH(SHADOWED), COST_POST(SHADOWED)
BOM 유효 리비전 선택, 스크랩 반영, 단위(UoM) 가정
- 소모 계산식(최소)

구성품 소요량
qty_component = goodQty × (bom_line.qty × (1 + scrap_rate))
반올림: 소수 6자리, Half-Up
단위: 최초엔 동일 단위(UoM) 가정(‘EA’). 단위 변환은 M9에서 보강.
- Outbox 페이로드 예시

{
  "workOrderId": "WO-123",
  "lines": [
    { "componentId": "RM-001", "qty": 10.000000, "uom": "EA" },
    { "componentId": "RM-002", "qty": 5.000000,  "uom": "EA" }
  ]
}

- Cost wouldSend 예시

{
  "workOrderId": "WO-123",
  "itemId": "I-0001",
  "totalQty": 10.0,
  "uom": "EA",
  "unitCost": 0,
  "currency": "KRW"
}

- DoD

실적 1건 → BACKFLUSH(SHADOWED) 1건, COST_POST(SHADOWED) 1건 생성
대사 리포트에 BACKFLUSH.SHADOWED 증가 반영
ERP 수신 0건(Shadow ON, wouldSend 로그만)
- 테스트

Perf 등록 → mes_outbox 2건 생성 확인
/internal/recon/run → BACKFLUSH.SHADOWED 증가
- 리스크/완화

BOM 리비전 미존재: 예외 스킵 + WARN(커서 전진 방해 금지)
단위 변환 미정: 동일 단위 가정 문서화 → M9에서 uom_conv 도입
## M7. ERP FE(C#) 1차 — 대시보드·Outbox 리스트·폼(지시/전이/실적)
- 목적

운영 검증판 확보. Shadow 배지/Outbox 상태 모니터링 + ERP 폼 3종 실행/멱등까지 눈으로 확인.
- 스코프

대시보드 카드 4종: SENT/RETRY/FAILED/SHADOW
Outbox 최근 100건 리스트(상태/유형/에러/생성시각)
폼: 지시 생성, 상태 전이(P→R→C), 실적 등록(멱등 헤더)
- 데이터 소스

ERP API(지시/전이/실적) — 8081
Outbox 카드/리스트 — MES DB 조회 또는 내부 API(권장: /internal/outbox)
- 내부 API(권장)

GET /internal/outbox?status=SENT&limit=100
POST /internal/outbox/{id}/retry
- DoD

카드/리스트 렌더, 폼 호출 2xx, 멱등 재생 결과 UI 확인
Shadow 배지(전역/엔드포인트) 표시

# DoD(확장)
카드 4종 수치 갱신 정상(폴링/수동)
리스트 필터/기간/검색 정상, 클릭 시 상세 툴팁(옵션)
폼 3종 2xx, 실적 멱등 재생 확인
Shadow 배지 표시
기간 기본값 7일로 설정되어 과거 이벤트까지 조회 가능
#

- 리스크/완화

권한/CORS: 내부 API는 내부 권한/네트워크로 제한
재전송 API는 M7.5에 분리 가능(스코프 관리)


# Milestones — 이후 계획 (WPF 화면 전환 + 경계 원칙 고정)
버전: v1.0  
작성일: 2025-09-03

## 컨텍스트
- 목표: 화면 채널만 WPF로 전환하면서, 기존 연동/운영 구조(MES Outbox·Shadow·증분)는 그대로 유지
- 경계 원칙(고정)
  - 쓰기: WPF → ERP(API) → Outbox → ERP(Shadow/Live). MES로 직행 X
  - 읽기: 계획/품목/BOM은 ERP 조회/증분 API 사용. Outbox·대사는 웹 관제에서 조회(필요 시 읽기 전용 임베드)

---

## 남은 마일스톤 개요
| 마일스톤 | 목적 | 예상기간 | 게이트(Go/No-Go) |
|---|---|---|---|
| M5’ 웹 관제 잠금 | 내부 관제 API(요약/목록/배지) 읽기 전용 확정 | 반나절 | /internal/** 접근정책 확정 + 응답 정합 |
| M6’ WPF Shell/클라이언트 | WPF 프로젝트 스캐폴딩 + 공통 HttpClient/헤더/UTC/멱등 유틸 | 1일 | ERP API 핑/토큰/UTC 변환/에러 바인딩 OK |
| M7’ WPF 화면 1차 | 지시 생성, 상태 전이(P→R→C), 실적 등록(멱등) | 1~2일 | 3폼 2xx + 에러 400 표준 포맷 표시 |
| M8’ 관제 연계 | Outbox·Shadow 관제(웹 임베드 or 요약 수치) | 반나절 | SHADOWED/SENT 수치 UI 반영 |
| M9’ ERP DB 영속화 | ERP 메모리→DB 전환(Items/BOM/Plans 최소셋) | 1일 | 재기동 후 데이터/증분/멱등 연속성 |
| M10’ 원가 기초(옵션) | cost simulate/post(Shadow) + 표준원가/UoM | 2일 | 시뮬 산식·금액 일관 + wouldSend 로깅 |

---

## M8’ 웹 관제 잠금(반나절)
- 목적
  - 내부 관제 API를 “읽기 전용”으로 확정하고 접근정책 문서화
- 범위
  - GET /internal/outbox/summary, GET /internal/outbox, GET /internal/config/shadow
  - dev=permitAll, stg/prod=내부망/ROLE_INTERNAL
- DoD
  - 3 API 응답 필드 고정(스키마 불변)
  - 접근정책 문서화 + 점검 체크리스트 저장
- 리스크/완화
  - FE 기간 필터 착시 → sinceMinutes 프리셋(60/1440/10080) 가이드

---

## M9’ WPF Shell/클라이언트(1일)
- 목적
  - WPF 앱 골격 + ERP 호출 공통 모듈(헤더/멱등/UTC/에러) 확보
- 범위
  - 설정: Erp.BaseUrl, Erp.ApiKey(보안 저장), Env(dev/stg/prod)
  - HttpClient: X-API-Key 기본 주입, 타임아웃/재시도 최소
  - 유틸: 멱등키(Guid per 요청), UTC 변환(표시는 로컬 토글), 표준 에러 바인딩(code/message)
- DoD
  - ERP 핑(헬스/샘플 POST) 2xx
  - 400 에러 바디 화면 표준 표시
- 리스크/완화
  - 키 하드코딩 금지 → Windows Credential Manager 사용 가이드

---

## M10’ WPF 화면 1차(1~2일)
- 목적
  - 핵심 3폼(지시/전이/실적) UX 완성, 멱등/UTC/검증이 화면에서 자연 동작
- 범위
  - 지시 생성: POST /erp/work-orders
  - 상태 전이: PUT /erp/work-orders/{id}/status (P→R, R→C만)
  - 실적 등록: POST /erp/performances (X-Idempotency-Key 필수)
  - 조회(간단): ERP 계획/품목/BOM 리스트 검색(필수 필드만)
- DoD
  - 각 폼 2xx 시 토스트, 400 시 표준 에러 바디 그대로 표시
  - 실적: goodQty>0이면 Outbox에 BACKFLUSH/COST_POST 생성(Shadow), goodQty=0은 미생성
  - 멱등: 동일 키 재전송 시 재생 확인(서버 응답 일관)
- 리스크/완화
  - 시간 입력(Z 누락) → 입력 컴포넌트에 UTC/Z 안내/검증

---

## M11’ 관제 연계(반나절)
- 목적
  - 운영자가 현황을 바로 확인할 최소 관제 뷰 제공(읽기 전용)
- 범위(택1 또는 병행)
  - 웹 관제 임베드: Outbox 요약·목록 화면 WebView로 삽입(내부망)
  - 요약 수치만 바인딩: GET /internal/outbox/summary → 카드 4종(SENT/RETRY/FAILED/SHADOWED)
- DoD
  - 실적 등록 후 SHADOWED 증가가 즉시/주기적으로 UI 반영
- 리스크/완화
  - 보안: 임베드 주소/쿠키 정책 문서화

---

## M12’ ERP DB 영속화(1일)
- 목적
  - ERP 재기동/증분/멱등 내구성 확보
- 범위
  - 테이블: items, bom_header/line(eff_from/eff_to), plan_line
  - 공통 컬럼: updated_at(UTC), is_deleted, 유니크 키(코드/리비전/라인키)
  - 리포지토리 교체: 메모리 → DB
- DoD
  - 재기동 후 데이터 유지
  - updatedSince·멱등 기록 연속성 유지
- 리스크/완화
  - 마이그레이션: upsert 스크립트 + 스냅샷 백업

---

## M13’ 원가 기초(옵션, 2일)
- 목적
  - 원가 계산 최소틀: simulate/post(Shadow), 표준원가/UoM 변환
- 범위
  - DB: tb_item_cost(item_id, cost_type=STD, amount, currency, eff_*), tb_uom_conv(from,to,factor)
  - API: POST /cost/simulate, POST /cost/post(Shadow)
  - 산식: qty_component = goodQty × (bom.qty × (1+scrap)), 반올림 6자리(Half-Up)
- DoD
  - 동일 입력 → 동일 금액(일관성), COST_POST wouldSend 누적
- 리스크/완화
  - 단위 혼재: 초기엔 동일 단위 가정, 변환은 필수 경로부터 적용

---

## 공통 게이트/체크리스트
- 에러 포맷: 400 바디 {code,message,path,method,timestamp} 일관
- 시간대: 모든 요청/응답 UTC ISO(…Z), UI는 로컬 토글 표시
- 멱등: X-Idempotency-Key 클라이언트 생성, 재시도 시 동일 키 유지
- Shadow: Backflush/Cost=ON, 202 + wouldSend(ERP 미전송 증빙)
- 보안: WPF는 ERP API만(쓰기). Outbox/대사는 웹 관제(읽기 전용)
- 증분/커서: 부분 성공 기준 커서 전진(루프 방지), Items/BOM/Plans OK

---

## 운영 위험/완화 요약
- WPF가 /internal/** 쓰기 호출 → 금지(읽기 임베드만 허용)
- 키/시간 실수 → 입력 가드(UTC/Z), 키 저장 정책 문서화
- sinceMinutes 착시 → FE 프리셋(기본 7일) 고정
- BOM 미적중/양품 0 → 큐 미생성 정상(UX 안내 문구 포함)

---

## 타임라인(제안)
| 주차/일 | 작업 | 메모 |
|---|---|---|
| D+0.5 | M5’ 웹 관제 잠금 | 접근정책/스키마 확정 |
| D+1.5 | M6’ WPF Shell/클라 | HttpClient/멱등/UTC/에러 바인딩 |
| D+3.5 | M7’ WPF 화면 1차 | 3폼 + ERP 조회(간단) |
| D+4.0 | M8’ 관제 연계 | 요약 카드 or 임베드 |
| D+5.0 | M9’ ERP DB(선택) | 영속화 전환 |
| D+7.0 | M10’ 원가 기초(선택) | simulate/post(Shadow) |

---

## 참고(시연 스크립트, 5분)
1) 작업지시 생성 → 상태 P→R → 실적(양품 10)  
2) Outbox SHADOWED(Backflush/Cost) 증가 확인(관제)  
3) 실적(양품 0) 재전송 → 큐 미생성(정책 설명)  
4) 계획/품목/BOM 조회(ERP API)  
5) Shadow 배지/정책 안내로 마무리
