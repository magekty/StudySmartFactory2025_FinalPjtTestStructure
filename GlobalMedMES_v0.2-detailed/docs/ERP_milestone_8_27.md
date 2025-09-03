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
- 옵션 B: 매핑 테이블
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
- 리스크/완화

권한/CORS: 내부 API는 내부 권한/네트워크로 제한
재전송 API는 M7.5에 분리 가능(스코프 관리)
## M8. ERP FE(C#) 2차 — BOM 뷰어/리비전/유효기간
- 목적

생산 시 BOM 근거를 FE에서 명확히 확인/수정(권한 범위 내). 리비전·유효기간 탐색을 쉽게.
- 스코프

BOM 헤더/라인 조회(리비전·eff_from/eff_to)
유효 리비전 선택(시점 기준)
라인 편집(옵션, 운영 정책에 따라 R/O로 시작 가능)
- API

GET /boms?updatedSince=...
GET /boms/{bomId}
(옵션) PUT /boms — 편집 허용 시
- DoD

특정 날짜·WO 기준 유효 BOM 확인 가능
편집은 정책 확정 전까지 R/O 권장
- 리스크/완화

순환 BOM/유효기간 겹침: 서버 검증 로직 필요(경고/차단)
변경 감사: modified_by/traceId 기록
## M9. 원가 기초 — item_cost/uom_conv + /cost/simulate·/cost/post
- 목적

원가 계산의 최소 기초: 표준원가/단위 변환 테이블 도입, 시뮬레이션·wouldSend API 확정.
- DB(최소)

tb_item_cost(item_id, cost_type, amount, currency, eff_from, eff_to)
tb_uom_conv(from_uom, to_uom, factor)
- API

POST /cost/simulate

{
  "workOrderId": "WO-123",
  "goodQty": 10.0,
  "bomId": "BOM-001",
  "asOfUtc": "2025-09-02T00:00:00Z"
}
- 응답

{
  "totalMaterialQty": 15.0,
  "totalMaterialCost": 12345.67,
  "currency": "KRW",
  "lines": [
    { "componentId":"RM-001","qty":10,"uom":"EA","unitCost":100,"amount":1000 },
    { "componentId":"RM-002","qty":5,"uom":"EA","unitCost":200,"amount":1000 }
  ]
}
POST /cost/post (Shadow 유지)
simulate 결과를 근거로 wouldSend 로깅(Outbox COST_POST)
- DoD

동일 파라미터로 재계산 시 일관된 금액
Shadow wouldSend 누적
- 리스크/완화

단위 변환 미스: uom_conv 필수 경로만 커버 → 점진 확장
원가 기준 다양성: cost_type=‘STD’부터 시작, 최근단가/평균단가는 후속
## M10. ERP DB 영속화 전환
- 목적

ERP 재기동/증분/멱등/감사를 위한 영속 스토리지 도입.
- 스코프

ERP DB 테이블: items, boms(header/line), plans(line)
공통 컬럼: updated_at(UTC), is_deleted, unique 키(코드/리비전/라인키)
멱등/updatedSince 일관성
- 이행

메모리 스토어 → Repository(DB) 교체
마이그레이션 스크립트/시드
증분/멱등 검증(재기동 내구성)
- DoD

ERP 재기동 후에도 데이터/updatedAt/멱등 기록 유지
MES 증분 연속성 OK
- 리스크/완화

시드 중복: unique 키 + upsert
타임존 혼선: UTC ISO 고정
## 부록: 공통 DoD/리스크/체크리스트
- 공통 DoD

모든 시간: UTC ISO(…Z)
표준 400 포맷 일관(code, message, path, method, timestamp)
스케줄러 단일화(@Scheduled 1개), 부분성공 커서 전진
/actuator/health=UP, /auth/login permitAll, Jwt 필터는 토큰 있을 때만
- 리스크/완화

커서 루프: try/catch로 실패 건 스킵 + processedMaxTs로 커서 갱신
FK 실패: ensureHeader(Plan→WO), Items 선행 동기
상태코드: 문자열 status_code + CHECK 제약 + 서비스 검증
- 체크리스트(릴리즈 전 5분)

Outbox SENT/RETRY/SHADOWED 정상
Plans/Items/BOM 증분 3종 반영 + 커서 최신
Backflush Shadow 202 + 대사값 반영
FE 카드/리스트/폼 동작 + 멱등 재생 OK
로그/알림 1회 수신
