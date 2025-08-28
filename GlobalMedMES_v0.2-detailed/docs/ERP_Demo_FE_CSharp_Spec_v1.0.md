docs/ERP_Demo_FE_CSharp_Spec_v1.0.md
# ERP Demo FE(C#) 사양 v1.0
작성일: 2025-08-27  
대상: ERP v0(최소 트러블, Shadow Mode) 운영·데모용 C# 기반 FE 콘솔

---

## 1) 목적/범위(확정)
- 목적: ERP v0 엔드포인트(계획/기준정보/지시/전이/실적/Backflush) 운영·데모·관제
- 범위:
  - 대시보드(Outbox 상태/에러/Shadow 현황)
  - 생산계획(등록/수정), 기준정보(품목/UoM/BOM) 조회
  - 작업지시(등록/상태 전이 P→R→C)
  - 실적 등록(UTC)
  - Backflush(표준 소모) 트리거(초기 Shadow 운용)
  - Shadow 설정(전역/엔드포인트별 토글)
  - 재전송/상세 로그 열람

---

## 2) 기술 스택(권장)
- ASP.NET Core 8 (LTS)
- Razor Pages(MVC 경량) + TagHelper(테이블/폼 재사용)
- Auth: Cookie + 기본 Admin/Viewer 롤(내장 메모리 or EF Core InMemory)
- Http 통신: HttpClient(typed client), Polly(지수 백오프·타임아웃)
- 빌드/배포: dotnet publish / Dockerfile / GitHub Actions(옵션)

---

## 3) 라우트/화면 구조
- GET /                        : 대시보드(카드: SENT/RETRY/FAILED/SHADOW, 최근 에러 10)
- GET /plans                   : 계획 목록(검색: planId/itemId/기간), 등록·수정 폼
- GET /items                   : 품목 목록(증분 동기 시각 표시), UoM 매핑 보기
- GET /boms                    : BOM 헤더/라인 트리(유효기간, revision)
- GET /work-orders             : 지시 목록(상태/기간/품목), 생성·전이(P→R, R→C)
- GET /performances            : 실적 목록(기간/설비/지시), 등록 폼(UTC)
- GET /backflush               : Backflush 트리거 페이지(Shadow 안내 배지)
- GET /outbox                  : Outbox 현황(필터: endpoint/status/Shadow, 재전송 버튼)
- GET /settings                : Shadow 전역/엔드포인트별 토글, API Key 마스킹, 제한값(31일/100) 표기

---

## 4) 페이지 상세 사양
### 4.1 대시보드(/)
- 카드: SENT, RETRY, FAILED, SHADOW 건수(+24h 추이 sparklines)
- 최근 에러 테이블: endpoint, code, message(요약), 발생시각, 재전송 링크
- Shadow 전역 상태 배지(ON/OFF), 엔드포인트별 카운트

### 4.2 계획(/plans)
- 테이블: planId, planLineNo, itemId, qty, dueDate(UTC), priority, updatedAt
- 폼: 등록/수정(UTC ISO only) → ERP→MES 전송(Shadow 영향 없음: 단순 계약 검증 목적)

### 4.3 기준정보(/items, /boms)
- Items: itemId, uom, updatedAt, 증분 동기 버튼
- BOMs: header(revision/effFrom/To), lines(componentId, qty, uom, scrap)
- UoM 맵: uom_erp, uom_mes, factor(DECIMAL 18,6, half up)

### 4.4 지시/전이(/work-orders)
- 생성: workOrderId(UUID), workOrderNumber, itemId, qty, status=P, startTs?(UTC)
- 전이: P→R, R→C만 허용(버튼 상태/서버 가드 동기)

### 4.5 실적(/performances)
- 등록: workOrderId, itemId, processId, equipmentId, goodQty, defectQty, start/end(UTC), idempotencyKey
- 목록: 기간/설비/지시 필터, UTC→KST 표시는 클라이언트 포맷

### 4.6 Backflush(/backflush)
- 트리거: workOrderId, mode=Shadow 배지 표기
- 안내: 초기 24~48h Shadow 권장(전환 기준 설명 텍스트)

### 4.7 Outbox(/outbox)
- 필터: endpoint(work-orders/status/performances/backflush/…), status(SENT/RETRY/FAILED/SHADOW)
- 컬럼: outboxId, endpoint, isShadow, validationOk, lastError, retryCount, nextRetryAt, createdAt
- 액션: 재전송(Shadow OFF일 때만 활성), 상세(원문 요약·마스킹·payloadHash)

### 4.8 설정(/settings)
- Shadow: 전역 토글 + 엔드포인트 개별 토글(SHADOW_WO/SHADOW_PERF/SHADOW_BACKFLUSH/SHADOW_COST)
- 제한: 기간 31일, 페이지 100, 타임아웃 5s, 레이트리밋 60rpm 표시
- 시크릿: ERP_API_KEY 마스킹, 저장 시 재시작 필요 여부 안내

---

## 5) 환경 변수 / 구성
- appsettings.json
  - ERP:
    - BaseUrl
    - ApiKey (v0)
  - Shadow:
    - Enabled (bool)
    - Endpoints: WorkOrders(bool), Performances(bool), Backflush(bool), Cost(bool)
  - Limits:
    - MaxRangeDays=31
    - PageSize=100
    - TimeoutMs=5000
    - RateLimitPerMin=60
  - Logging:
    - MaskFields: ["apiKey","token","price","pii.*"]

---

## 6) 보안/권한
- FE 인증: Cookie(Login 페이지), Roles: Admin/Viewer
- 권한:
  - Viewer: 조회만, 재전송/토글 비활성
  - Admin: 생성/전이/실적/Backflush 트리거/재전송/토글 사용
- CSRF: AntiForgery 토큰
- 로깅 마스킹: ApiKey/Token/민감 필드 규칙 기반 마스킹

---

## 7) Shadow Mode 연동(프런트 관점)
- 상태 표기: 전역/엔드포인트별 배지
- Outbox 행 isShadow=true면 “SHADOWED” 뱃지, wouldSend 표시
- 전환 UX: 토글 변경 → 확인 모달(전환 기준 요약) → 저장 후 재로딩

---

## 8) 제약/검증 규칙(프런트 레벨)
- 시간 입력: UTC ISO(…Z) 형식만 전송, 브라우저 표시는 KST
- 기간/페이지: from~to 최대 31일, size 최대 100
- 상태맵: P/R/C 외 값 차단(P→R→C만 버튼 활성)
- 멱등키: 실적/Backflush 전송 시 필수(UUID 자동 생성)

---

## 9) 성능/관측
- 리스트 가상스크롤/페이지네이션(100 상한)
- 지수 백오프 재시도(Polly) + 타임아웃 5s
- 메트릭: 전송 소요, 실패율, Shadow 비중(옵션 Prometheus exporter)

---

## 10) CI/CD
- 빌드: dotnet build/publish -c Release
- Dockerfile: ASP.NET 8 base, 환경변수 주입
- 워크플로: main push → build → docker image → stg 배포

---

## 11) DoD
- 필수 페이지 8종 동작(대시보드/계획/품목/BOM/지시/전이/실적/Outbox/설정)
- Shadow 토글/표기 정상, 재전송 동작(Shadow OFF에서만)
- 제한/검증 규칙(UTC/31일/100/멱등) 일관 적용
- 민감정보 마스킹 로그 확인

---

## 12) 리스크/완화
- 과도한 기능 확장 → MVP 화면 우선(표·폼·토글)
- Shadow/Sent 혼선 → 배지/확인 모달/이력 로그
- 로그 용량 증가 → 보관주기 30~90일, 집계 테이블 분리

---