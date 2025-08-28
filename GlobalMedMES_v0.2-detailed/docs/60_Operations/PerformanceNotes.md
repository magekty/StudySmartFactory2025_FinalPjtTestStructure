# Performance Notes
- 핵심 인덱스: (equipment_id,start_time), (work_order_id,start_time), KPI 유니크(일×설비×공정×품목)
- KPI 7일 쿼리: range scan, EXPLAIN 점검
- 슬로우쿼리: 500ms 이상 캡처
