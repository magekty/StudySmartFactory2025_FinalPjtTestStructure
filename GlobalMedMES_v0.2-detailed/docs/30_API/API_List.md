# API 목록(v1)
공통: Authorization: Bearer <jwt> 또는 세션 쿠키, 시간=ISO8601 UTC("2025-08-10T09:00:00Z")

- POST   /auth/login               (공개)
- GET    /menus/my                 (로그인)
- POST   /work-orders              (ROLE_OP+)
- GET    /work-orders              (ROLE_OP+)
- GET    /work-orders/{id}         (ROLE_OP+)
- PUT    /work-orders/{id}/status  (ROLE_OP+)
- POST   /equip-status             (ROLE_OP+)     # RUN 등록
- POST   /performances             (ROLE_OP+)
- GET    /performances             (ROLE_OP+)
- GET    /kpi-targets              (ROLE_OP/QA/ADMIN)
- GET    /kpi/actuals              (ROLE_OP/QA/ADMIN)

예시 요청 - 실적 등록
{
  "workOrderId":"WO-UUID",
  "itemId":"I-UUID","processId":"P-UUID","equipmentId":"E-UUID",
  "producedQty":100.0,"defectQty":5.0,
  "startTime":"2025-08-10T09:00:00Z","endTime":"2025-08-10T09:30:00Z"
}
