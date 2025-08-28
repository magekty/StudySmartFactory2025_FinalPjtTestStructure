# API_Validation_v1 (엔드포인트별 검증/오류/성능)

## 공통
- 시간 포맷: ISO8601 UTC("2025-08-10T09:00:00Z")
- 에러 포맷:
{
  "code":"...", "message":"...", "details":{...},
  "traceId":"...", "timestamp":"...", "path":"/...", "method":"POST"
}

## POST /work-orders (지시 생성)
- 권한: ROLE_OP+
- 필드 검증
  - workOrderNumber: 필수, UK
  - itemId/processId/equipmentId: FK 존재
  - orderQty: number, ≥0
- 오류: VALIDATION_ERROR(400), DUPLICATE_KEY(409), FORBIDDEN(403)

## PUT /work-orders/{id}/status (상태 전이)
- 권한: ROLE_OP+
- 필드 검증
  - toStatus ∈ {R,C}
  - 전이표 준수(P→R, R→C)
- 오류: WO_STATUS_INVALID(400), FORBIDDEN(403)

## POST /equip-status (RUN 등록)
- 권한: ROLE_OP+
- 필드 검증
  - equipmentId: FK
  - statusCode ∈ {RUN,IDLE,DOWN}
  - startTimeUtc: 필수
  - endTimeUtc: 선택, 있으면 end≥start
- 오류: TIME_ORDER_INVALID(400), VALIDATION_ERROR(400), CODE_INACTIVE(400)

## POST /performances (실적 등록)
- 권한: ROLE_OP+
- 필드 검증
  - workOrderId: FK, WO 상태=R
  - producedQty: number, ≥0
  - defectQty: number, 0≤defect≤produced
  - startTime/endTime: end≥start
- Tx: PERF insert → WO 누적 생산/불량 합산 갱신(한 Tx)
- 오류: VALIDATION_ERROR(400), TIME_ORDER_INVALID(400), FORBIDDEN(403)

## GET /kpi/actuals (목표 vs 실적)
- 권한: ROLE_OP/QA/ADMIN
- 파라미터: kpiDate, equipmentId (필수)
- 성능: SLA P95<500ms, EXPLAIN range 사용

## API/서비스 규칙 9건(개발 중 체크 지침)
형태: Postman/Newman 컬렉션으로도 바로 변환 가능. 각 항목은 “요청 → 기대 HTTP/Body → 핵심 assertion”만 간결·명확히.
#	엔드포인트	시나리오	기대
1	POST /auth/login	올바른 자격	200, token 발급, roles 배열 포함
2	POST /auth/login	잘못된 비번	401 AUTH_REQUIRED
3	GET /menus/my	미로그인 접근	401 AUTH_REQUIRED
4	POST /work-orders	정상 생성(P)	201, status=P, WO 번호 UK 유지
5	PUT /work-orders/{id}/status	P→R	200, status=R
6	PUT /work-orders/{id}/status	R→C	200, status=C
7	PUT /work-orders/{id}/status	C→R	400 WO_STATUS_INVALID
8	POST /equip-status	RUN 정상 등록	201, 최근 상태 카드 반영(조회시 상단)
9	POST /performances	100/5, end≥start	201, goodQty=95, WO 누적 증가
샘플 요청/검증 예시(발췌)

http


# 5) P->R
PUT /work-orders/WO-0001/status
Content-Type: application/json
Authorization: Bearer <token>
{ "toStatus":"R" }
# expect: 200, body.status == "R"

# 7) C->R 금지
PUT /work-orders/WO-0001/status
{ "toStatus":"R" }
# (사전: 현재 C 상태로 세팅) expect: 400, code == "WO_STATUS_INVALID"

# 9) 실적 등록
POST /performances
{ "workOrderId":"WO-0001","itemId":"I-0001","processId":"P-0001","equipmentId":"E-0001",
  "producedQty":100,"defectQty":5,
  "startTime":"2025-08-10T09:00:00Z","endTime":"2025-08-10T09:30:00Z" }
# expect: 201, body.goodQty == 95, 그리고 /work-orders/{id} 조회시 누적 증가
핵심 assert 팁

상태 전이: 응답 status 필드, 에러 code 문자열까지 확인
실적: 응답 goodQty(있으면) 또는 (produced-defect) 계산 검증
누적: 실적 후 /work-orders/{id}에서 produced_qty 누적 상승 검증


## 권한·메뉴 4건(개발 중 체크 지침)
목적: RBAC/메뉴 가드가 FE/BE 모두 일관 동작하는지 검증(“보여주지 않기 + 호출해도 막는” 이중 가드)
#	검사 항목	절차	기대
A1	메뉴 필터(ROLE_OP)	로그인 후 /menus/my → 사이드바 구성	Admin 메뉴/버튼 미표시
A2	엔드포인트 가드(ROLE_OP)	POST /kpi-targets	403 FORBIDDEN
A3	엔드포인트 가드(비로그인)	GET /work-orders	401 AUTH_REQUIRED
A4	버튼 가시성	KPI 보드에서 “목표 등록” 버튼	ROLE_ADMIN만 표시
지침

FE: 메뉴는 /menus/my 결과로만 렌더. 버튼은 allow_write/exec 플래그로 제어
BE: 컨트롤러 단 권한 어노테이션/인터셉터로 401/403 방어