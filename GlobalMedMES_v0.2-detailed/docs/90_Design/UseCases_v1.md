# UseCases_v1 (v1 버전 B, 상세)

## UC-01 로그인/메뉴 권한
- Actor: 사용자(ROLE_OP/QA/ADMIN)
- Trigger: 로그인 화면에서 ID/비밀번호 입력
- Pre: 계정 is_active=1, locked_until 과거
- Main:
  1) /auth/login에 자격 제출 → 서버 검증(비번 해시 비교)
  2) JWT(Access 30m) + 세션(2h) 발급, 실패 카운트 리셋
  3) /menus/my로 권한 기반 메뉴 트리 로드
  4) 네비게이션 렌더 및 대시보드 이동
  5) 감사로그 AUTH_LOGIN 기록
- Alt:
  - 비번 오류: 401 + 실패 카운트+1(정책 임계 도달 시 locked_until 설정)
- Post: traceId 포함 표준 로그 기록

## UC-02 작업지시 발행/조회/상태(P→R→C)
- Actor: 작업자(ROLE_OP), 관리자(ROLE_ADMIN)
- Trigger: "지시 생성" 클릭
- Pre: ITEM/PROCESS/EQUIPMENT 존재, WO 번호 중복 없음
- Main(발행):
  1) itemId/processId/equipmentId/orderQty 입력
  2) 검증(orderQty≥0, FK 존재)
  3) TB_WORK_ORDER insert(status=P)
  4) 목록/상세 갱신
  5) 감사로그 WO_CREATE
- Main(상태변경):
  1) 상세→ "R" 또는 "C" 선택
  2) 전이표 검증(P→R, R→C만 허용)
  3) 상태 갱신 및 감사로그 WO_STATUS_CHANGE
- Alt:
  - 전이 위반: 400 WO_STATUS_INVALID
- Post: WO 상태/감사 일치

## UC-03 설비 상태 RUN 등록(교대 표시)
- Actor: 작업자(ROLE_OP)
- Trigger: "RUN 시작" 버튼
- Pre: 교대 캘린더(설비 or 작업장) 존재, XOR 충족
- Main:
  1) equipmentId, startTimeUtc 제출
  2) TB_EQUIPMENT_STATUS_LOG insert(status=RUN)
  3) 교대 뱃지 표시 및 최근 상태 카드 갱신
  4) 감사로그 EQP_STATUS_CREATE
- Alt:
  - 시간 역전 요청(end<start): 400 TIME_ORDER_INVALID
- Post: 설비×시간 인덱스 경로로 즉시 조회 가능

## UC-04 실적 등록(양품/불량, 시간)
- Actor: 작업자(ROLE_OP)
- Trigger: "실적 등록" 버튼
- Pre: 대상 WO 상태=R
- Main:
  1) produced/defect/start/end 입력
  2) 검증(defect≤produced, end≥start, FK 존재)
  3) PERF insert → 같은 Tx로 WO 누적 생산/불량 갱신
  4) 토스트/상세 반영
  5) 감사로그 PERF_CREATE
- Alt:
  - 검증 실패: 400 VALIDATION_ERROR/TIME_ORDER_INVALID
- Post: KPI 집계에 즉시 반영 가능

## UC-05 KPI(목표 vs 생산량·수율) 확인
- Actor: OP/QA/ADMIN
- Trigger: KPI 보드 진입
- Pre: KPI_TARGET 존재
- Main:
  1) kpiDate, equipmentId 필터
  2) 목표/실적 집계 조회
  3) 카드 3종(목표·실제 생산량/수율) + 색상 규칙 적용(초록/노랑/빨강)
- Alt:
  - 데이터 없음: 빈 상태 UI
- Post: EXPLAIN range 확인, SLA<500ms 충족
(Generated: 2025-08-09 10:18:30 UTC)
