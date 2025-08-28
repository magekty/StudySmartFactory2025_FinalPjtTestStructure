# Runbook
- ENV: DB_URL, JWT_SECRET, SESSION_SECRET
- JWT 만료 30분, 세션 2시간(비활성 만료)
- 시간: 요청/응답 UTC, UI는 KST 렌더
- 데모 리셋: Seeds_Reset.sql 실행(관리자 전용)
- 로깅: traceId 포함, 에러 포맷 표준 유지
