# Sec_Scan_Runbook (간단 취약점 점검)

## 의존성 취약점
- 도구: OWASP Dependency Check 또는 Snyk
- 정책: 중급↑ 발견 시 파이프라인 Fail

## 정적 분석(라이트)
- 룰: 하드코딩 시크릿 탐지, 위험 API 패턴 금지
- 결과: 리포트 저장(경미는 경고)

## 시크릿/세션
- .env(gitignore), 배포는 OS Secret/Vault
- 세션 쿠키: HttpOnly, Secure, SameSite=Lax
- CSRF: 세션 경로 보호(토큰), JWT GET 예외
