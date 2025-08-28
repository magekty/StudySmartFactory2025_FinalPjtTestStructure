# Client Guards & Network

- 네트워크 안정화
  - axios 인스턴스: Authorization 부착, 401은 hadAuth 요청에만 logout
  - React Query: enabled: !!token, 401 재시도 금지, refetchOnWindowFocus=false
- 권한 가드
  - 라우트: <PermRoute require="write"> (쓰기 전용 페이지 하드 가드)
  - 버튼: <Can write>…</Can> 또는 <GuardButton require="write" />
  - 경로 prefix 매칭: /a/b/c → /a/b 권한으로 허용
- 실적 등록 UX/가드
  - canSave = 권한(write) ∧ 필드/수량/시간 유효 ∧ status=R ∧ start/end ≥ baseline
  - baseline = startTs ?? createdAt (API는 UTC)