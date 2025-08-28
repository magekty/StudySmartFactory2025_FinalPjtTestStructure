# Telemetry over MQTT (Draft)

- 브로커: TODO(host/port/auth)
- 토픽 네임스페이스(예): factory/{lineId}/{equipmentId}/status
- 샘플 페이로드(JSON, 초안)
{
  "equipmentId": "E-0001",
  "status": "RUN",
  "speed": 120,
  "count": 3456,
  "ts": "2025-08-18T02:01:18Z"
}
- 화면 반영: 설비 상태 보드(상태/속도/알람), 목표 p95 3–5초 내 갱신
- TODO
  - 실제 토픽/인증 방식
  - 최종 페이로드 필드 정의