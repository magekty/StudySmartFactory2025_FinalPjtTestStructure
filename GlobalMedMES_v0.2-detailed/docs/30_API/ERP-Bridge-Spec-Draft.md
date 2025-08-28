# ERP Bridge Spec (Draft)

- 구현 방식: C# 응용프로그램 → REST API(동기)
- 목적: 지시 생성/전이/완료, 생산/불량, 기준정보(품목/설비) 최소 동기
- 엔드포인트(초안)
  - MES→ERP: POST /erp/work-orders, PUT /erp/work-orders/{id}/status, POST /erp/performances
  - ERP→MES: GET /mes/items, GET /mes/equipments
- 공통 규칙
  - 시간: UTC ISO(…Z)
  - 상태코드: P/R/C 매핑표 유지
- TODO
  - 교환 필드 목록 확정(필수/옵션)
  - 오류/재시도 정책(HTTP 코드, 보류 큐)