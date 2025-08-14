# GlobalMed MES — MESA-11 기반 스텐트 제조 스마트 MES 플랫폼

## 비전
“스텐트 의료기기 제조의 핵심 공정을 효율적이고 신뢰성 있게 관리하는 스마트 MES”

## v1 시나리오(버전 B, 강화형)
로그인/권한 → 작업지시 → 설비 상태 로그 RUN(교대 일부 표시) → 실적 등록 → KPI(목표 vs 생산량·수율)
- OEE의 Availability는 후속 확장 예정

## 범위
- Must: Auth/RBAC, WO, EQP RUN, PERF, KPI(3카드)
- Should: 상태 타임라인(최근 3건), ERP 링크 뱃지(자리표시)
- Non-goals: 가동률 실집계·보전·외부 연동 실제 동작

## 성공 기준
- 데모 100% 완주, SLA: 실적<300ms, KPI(7일)<500ms
- db_lint critical=0(major는 화이트리스트만)

## 일정(40일)
W1 인증/RBAC/시드/린트 → W2 지시/상태/실적 → W3 KPI 보드 → W4~5 안정화/PPT → W6 리허설
(Generated: 2025-08-09 07:01:43 UTC)
