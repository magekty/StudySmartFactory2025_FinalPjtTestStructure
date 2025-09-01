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

# M5. ERP 콘솔 FE(C# Razor) 1차(1일)
- 산출물
대시보드 카드(SENT/RETRY/FAILED/SHADOW), Outbox 리스트(재전송), Shadow 토글, 지시/전이·실적 폼
- DoD
전역/엔드포인트 Shadow 배지 표시, 재전송(지시/실적만) 동작
- Gate
관제에서 전환/재전송 테스트 OK면 M6
# M6. 대사·알림·전환(반나절)

- 산출물
일일 02:00 UTC 대사(지시/실적/소모 합계) + 알림
전환 기준표: validation 실패율 <0.5%, RETRY/FAILED 0, 대사 불일치 0 → 지시/실적 Live 유지, Backflush/Cost Shadow 유지
- DoD
첫 리포트 생성·알림 수신 확인
- Gate
기준 충족 시 “시연 가능” 상태 확정

# M7. 품질 보강(선택, 1일)
- 산출물
Swagger 전역 헤더(X-API-Key, X-Idempotency-Key)
응답 헤더 X-Idempotency-Replayed 표식(모든 엔드포인트)
캐시 키 범위 확장(method:path:key)
- DoD
Swagger에서도 멱등 재생 식별 가능, 키 충돌 없음
—

## 하루 실행 루틴(요약)
Day 1: M1
Day 2: M2
Day 3: M3
Day 4: M4 + M5(절반 병행)
Day 5: M5 마감 + M6
(+1d 버퍼로 M7 품질 보강)
## 스모크 체크(매일 끝에)
지시 생성 201 → 같은 키 재전송 동일 201(바디 동일)
전이 200 → 같은 키 재전송 동일 200
실적 201 → 같은 키 재전송 동일 201
Backflush 202(Shadow) → 대사 집계 반영
오류 케이스: UOM_MAPPING_MISSING, BOM_NOT_EFFECTIVE, TIME_ORDER_INVALID, DUPLICATE_KEY 확인