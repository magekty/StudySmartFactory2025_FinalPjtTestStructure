# Perf_Test (측정 계획/절차)

## 대상·SLA
- GET /performances?equipmentId&from&to → P95 < 300ms
- GET /kpi/actuals?kpiDate&equipmentId → P95 < 500ms

## 방법
- JMeter: VU 50/100, 1분 샘플, think time 200~500ms
- DB: EXPLAIN 캡처(인덱스 키: (equipment_id,start_time), (work_order_id,start_time))

## 예시 SQL
EXPLAIN SELECT * FROM TB_PRODUCTION_PERFORMANCE
WHERE equipment_id=:eqp AND start_time BETWEEN :from AND :to
ORDER BY start_time;
