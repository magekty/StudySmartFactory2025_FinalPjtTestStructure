# StateMachine (작업지시/설비 상태)

## 작업지시 상태 전이 - 생산, 실행, 완료
| From | To | 허용 | 규칙 |
|---|---|---|---|
| P | R | 예 | 최초 시작 시 |
| R | C | 예 | 누적 생산>0 권장(옵션) |
| C | R/P | 아니오 | WO_STATUS_INVALID |

- 위반 시: 400 WO_STATUS_INVALID
- 권한: ROLE_OP+

## 설비 상태 이벤트 규칙
- 상태코드: RUN/IDLE/DOWN
- 시간: start_time ≤ end_time(null 허용)
- 교대: XOR(설비 또는 작업장) + 부분 유니크(중복 차단)
- 겹침: 허용(시뮬/현장 차이); 린트에서 겹침 탐지 보고
