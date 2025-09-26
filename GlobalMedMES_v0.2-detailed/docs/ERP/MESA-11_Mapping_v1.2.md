# docs/MESA-11_Mapping_v1.2.md
# MESA‑11 기반 9대 목표 달성 현황 보고서 v1.2
작성일: 2025‑08‑26

각 항목은 실무 정의 → 현재 구현(핵심 근거) → 달성 수준 → 리스크/다음 액션 순으로 기술합니다.

---

## 0. 공통 운영 원칙(변경 금지)

- 시간 표준
  - API 응답/요청: UTC ISO(…Z)
  - 화면 표기: KST
  - 비교/검증: UTC ms (Date.getTime)
  - 실적 기준선(finalBaseline) = max(지시 baseline, 해당 지시의 마지막 실적 종료시각)
    - baseline = startTs ?? createdAt

- 권한 표준
  - 서버: HTTP 규칙(예: POST /performances=ROLE_ADMIN/OP) + @PreAuthorize + permChecker(prefix)
  - 클라이언트: 라우트 가드(require="write") + 버튼 가시성(GuardButton/CanWrite)

- 멱등/안정성
  - 실적 등록: requestId UNIQUE + 사전 조회 + 유니크 캐치로 중복 완전 차단
  - 에러 포맷: { code, message, path, method, timestamp }, 401/403/409/400/500 구분
  - CORS: http.cors 연결, localhost/127.0.0.1 허용, OPTIONS /** permitAll

- 스모크 자동화(고정)
  - 케이스 6종: viewer 403, op 201, DUPLICATE_KEY, TIME_ORDER_INVALID, WO_STATUS_INVALID, PERF_BEFORE_WO
  - finalBaseline 계산 반영(= baseline vs 마지막 실적 종료시각 중 최대값)

---

## 1. 9대 목표 매핑(요약 표)

![alt text](imgs/9MVP_mapping.png)

---

## 2. Detailed Scheduling(세부 스케줄링)

- 실무 정의  
  계획 대비 실제 착수/완료 타이밍 관리, 우선순위·슬로팅

- 현재 구현(핵심 근거)
  - P→R→C 상태 게이트로 실행 타이밍 명확화
  - R 상태 행에서만 실행/실적/전이 버튼 노출(서버/FE 가드 일치)

- 달성 수준: 부분

- 리스크/다음 액션
  - 상세 우선순위/슬롯 미도입 → ERP 계획 priority 반영, R 전이 시점 리포트

---

## 3. Operations Dispatching(디스패칭/투입)

- 실무 정의  
  투입(Release)/완료(Close) 제어 및 오류 방지

- 현재 구현
  - P→R(투입), R→C(완료)만 허용(역전 금지)
  - 서버 HTTP 규칙 + 메서드 가드 + FE 라우트/버튼 가드 일치

- 달성 수준: 완료

- 리스크/다음 액션
  - 대량 전이 시 멱등·재시도 고려 → 일괄 전이 API 설계

---

## 4. Resource Allocation & Status(자원/설비 상태)

- 실무 정의  
  설비 가동/유휴/다운 상태 가시화와 대응

- 현재 구현
  - 설비 상태 RUN/IDLE/DOWN 로그, 최근 상태 보드(라이트)
  - UTC 조합 등록, 기간 필터(From/To), 목록/정렬

- 달성 수준: 부분

- 리스크/다음 액션
  - 실시간 반영/알람 부족 → MQTT 수집, 다운 N초 경보, 상태 타일 경과 타이머

---

## 5. Data Collection(생산/실적 데이터 수집)

- 실무 정의  
  정확한 생산량/불량/시간 수집 및 중복/조기/역전 방지

- 현재 구현
  - finalBaseline 이후만 실적 허용  
    (finalBaseline = max(startTs ?? createdAt, 마지막 실적 종료시각))
  - 시간/수량 검증(end<start, defect>produced 차단)
  - 멱등키: requestId UNIQUE + 사전 조회 + 유니크 캐치

- 달성 수준: 완료

- 리스크/다음 액션
  - 대량 처리/취소 요구 발생 시 전용 API 추가

---

## 6. Quality Management(품질)

- 실무 정의  
  수율/불량 관리, 기준·판정 적용

- 현재 구현
  - defectQty/수율 기반 KPI 카드(라이트)(예정)

- 달성 수준: 부분

- 리스크/다음 액션
  - 불량 유형 코드 연동, 공정별 품질 규칙·판정 추가

---

## 7. Process Management(공정 관리/제어)

- 실무 정의  
  공정 기준 준수, 순서·시간 제약

- 현재 구현
  - 상태 게이트 + 기준선 가드로 순서·시간 통제
  - UTC ms 비교(서버/클라 일관)

- 달성 수준: 완료(라이트)

- 리스크/다음 액션
  - 레시피/파라미터·변경이력(승인/감사) 확장

---

## 8. Product Tracking & Genealogy(추적/계보)

- 실무 정의  
  “누가/언제/어디서/무엇을” + 자재 LOT 계보

- 현재 구현
  - WO–Item–Process–Equipment 이력·시간 정합

- 달성 수준: 부분

- 리스크/다음 액션
  - Backflush에서 Actual 소모로 확장 시 LOT 계보 연결

---

## 9. Performance Analysis(성과 분석)

- 실무 정의  
  생산성/수율/OEE 등 KPI 분석·시각화

- 현재 구현
  - KPI(출력/수율) 기초, UTC→KST 표준으로 BI 연계 용이(예정)

- 달성 수준: 부분

- 리스크/다음 액션
  - Data Mart 설계(fact 실적 + dim 설비/품목/일자)
  - Power BI v1(일/주/월 + 설비/라인 필터)

---

## 10. Standard Operations / Security & Authority(표준 운영/권한/보안)

- 실무 정의  
  표준화된 권한·보안, 일관된 오류처리

- 현재 구현
  - 서버 3중 가드(HTTP 규칙 + @PreAuthorize + permChecker(prefix))
  - 클라 라우트(require="write") + 버튼(GuardButton/CanWrite) 일관
  - 401/403/409/400/500 JSON 표준, CORS 프리플라이트 정상

- 달성 수준: 완료

- 리스크/다음 액션
  - 서버‑서버 연동(ERP)용 서비스 계정/토큰 분리 운영

---

## 11. 교차 정책 요약(시간/권한/멱등/오류/CORS)

- 시간: 입력·출력 UTC ISO only, 화면 KST, 비교/검증 UTC ms
- 권한: 서버 규칙 + 메서드 가드 + 라우트/버튼 3중 일치
- 멱등: 실적 requestId UNIQUE, 연동 단계 Outbox/Inbox 예정
- 오류: 코드·메시지 표준화, 스택 숨김, 사용자 메시지 단순화
- CORS: cors() 연결, 정확 Origin, allowCredentials=true, OPTIONS permitAll

---

## 12. 리스크 레지스터(요약)

- 시간대 불일치(외부 연동): UTC ISO only 계약 문구, OffsetDateTime.parse
- finalBaseline 충돌(ERP 전송): MES 상세에 finalBaseline 제공(권장) 또는 Validate API
- 상태맵/UoM 혼선: 코드·UoM 매핑 테이블/문서, DECIMAL 스케일·반올림 규칙
- 과다 조회: 기간·페이지 제한, 인덱스(start_time, equipment_id), 레이트 리밋
- 멱등/재시도: Idempotency-Key 필수, Outbox/Inbox(연동) 적용

---

## 13. 테스트 전략

- 스모크(자동)
  - 케이스 6종: viewer 403, op 201, DUPLICATE_KEY, TIME_ORDER_INVALID, WO_STATUS_INVALID, PERF_BEFORE_WO
  - finalBaseline= max(지시 baseline, 마지막 실적 종료시각) 반영

- 컨트랙트 테스트(연동)
  - ERP API 스키마 상호 검증(UTC·멱등·에러 포맷)

- E2E
  - 계획→지시→전이(P→R)→실적→Backflush→전이(R→C) 단일 흐름

---

## 14. 용어 사전

- P/R/C: Planned/Released/Closed(작업지시 상태)
- baseline: startTs ?? createdAt
- finalBaseline: max(baseline, 마지막 실적 종료시각)
- Backflush: 표준 소모 기반 자재 차감
- Actual Consumption: 실제 소모 기반 자재 차감
- Idempotency-Key: 동일 키 재요청 시 중복 없이 동일 결과 보장

---

## 15. DoD(완료 기준) 체크

- [완료] 지시 전이(P→R→C)·실적 등록 가드(상태/시간/멱등) 일관 동작
- [완료] 서버 3중 가드·라우트/버튼 가드·JSON 에러·CORS 안정
- [완료] 스모크 6케이스 ALL PASS
- [부분] 설비/HMI 보드·품질·추적/계보·KPI/BI(라이트) → 확장 로드맵 수립

---

## 16. 다음 액션(권장 우선순위)

1) ERP v0 연동  
- ERP→MES: 생산계획/BOM·품목 증분  
- MES→ERP: 지시/전이/실적/Backflush 멱등 전송(Outbox/재시도 포함)

2) BI v1  
- Data Mart 설계 + KPI 3종 리포트

3) 텔레메트리 v0  
- MQTT 수집→상태 보드 3~5초 내 반영 + 다운 N초 알람

4) CI/CD  
- 스모크 자동 실행, 실패 알림

---