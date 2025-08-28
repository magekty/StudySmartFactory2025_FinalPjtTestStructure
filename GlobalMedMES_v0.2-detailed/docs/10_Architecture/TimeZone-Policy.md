# Time Zone Policy

- 저장 표준
  - DB: DATETIME(UTC) 보관
- API 응답
  - 모든 시간 필드: UTC ISO 8601(예: 2025-08-18T02:01:18Z)
- 프런트 처리
  - 표시: KST 고정(toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' }))
  - 입력→전송: 로컬(KST) → UTC ISO 변환(new Date(`${date}T${time}:00`).toISOString())
  - 비교/가드: getTime() 기반(UTC ms)
- 실적 등록 기준선(baseline)
  - baseline = startTs ?? createdAt
  - 등록 시 start/end ≥ baseline, 지시 상태 = R
- 주의(재발 방지)
  - FE에서 임의 ‘Z’ 덧붙이기 금지
  - 서버는 라벨 일관(UTC ‘Z’) 유지