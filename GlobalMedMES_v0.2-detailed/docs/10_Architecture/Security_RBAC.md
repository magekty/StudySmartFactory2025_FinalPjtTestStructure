# Security & RBAC
- 역할: ROLE_OP / ROLE_QA / ROLE_ADMIN
- 메뉴 권한: ROLE_MENU.allow_read/write/exec 플래그
- 인증: JWT(Access 30분) + 세션(2시간, 비활성 시 만료) 병행
- CSRF: 세션 경로 보호, JWT 경로는 GET 예외 허용(정책)
