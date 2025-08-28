# GlobalMed MES — v1 (Version B)

MESA-11 기반 스텐트 제조 스마트 MES. v1 시나리오(로그인→지시→설비 상태 RUN→실적→KPI)를 40일/4인 팀으로 완주할 수 있게 설계/환경/테스트를 표준화했습니다.

## 문서 목차(개발자용 설명 포함)

- 00_Overview
  - docs/00_Overview/OnePager.md — 프로젝트 한 장 요약(목표/범위/일정/성공기준)
- 10_Architecture
  - docs/10_Architecture/ADRs.md — 아키텍처 결정 기록(UTC, RESTRICT, code_id 등 불변 원칙)
  - docs/10_Architecture/Security_RBAC.md — 역할/권한/메뉴 가드 설계 요약
- 20_DataModel
  - docs/20_DataModel/ERD_v1.png — 데이터 모델 그림(핵심 테이블 관계)
  - docs/20_DataModel/Schema_v1.sql — DB 스키마 SQL(테이블/제약/인덱스)
  - docs/20_DataModel/CodeDictionary.md — 코드 그룹/코드 목록(STATUS/TYPE 등)
- 30_API
  - docs/30_API/API_List.md — API 엔드포인트 목록(요청/응답 요약)
  - docs/30_API/ErrorSpec.md — 표준 에러 포맷/샘플(코드·메시지·traceId 등)
  - docs/30_API/Swagger.yaml — Swagger(OpenAPI) 정의 파일(문서/테스트용)
  - docs/30_API/API_Test9.md — API 뼈대 개발 테스트
- 40_UIUX
  - docs/40_UIUX/UserFlows.md — 화면/사용자 흐름(로그인→지시→RUN→실적→KPI)
  - docs/40_UIUX/Wireframes_Notes.md — 와이어프레임 설명(필드/컴포넌트 메모)
  - docs/40_UIUX/MenuMatrix.md — 메뉴-권한 매트릭스(누가 어디에 접근 가능한가)
- 50_QA
  - docs/50_QA/DoD_Acceptance.md — 수용 기준 체크표(기능/코드/문서/성능/보안)
  - docs/50_QA/TestCases.md — 핵심 테스트 케이스 목록(성공/실패 시나리오)
  - docs/50_QA/db_lint_report.md — DB 린트 결과 리포트(critical=0 확인용)
- 60_Operations
  - tools/sql/wipe_all.sql — 개발 DB 전체 초기화(SQL TRUNCATE 일괄)
  - tools/sql/dev_seed_plus_ordered.sql — 확장 더미 데이터 시드(메뉴/RBAC 포함)
- 70_Demo
  - docs/70_Demo/DemoScript.md — 3~5분 데모 스크립트(발표용 진행 순서)
  - docs/70_Demo/Slides_Outline.md — 발표 슬라이드 구성(10장 골격)
- 90_Design
  - docs/90_Design/UseCases_v1.md — 유스케이스 5종(로그인/지시/상태/실적/기준)
  - docs/90_Design/StateMachine.md — 작업지시 상태 전이표(P→R→C, 금지 전이)
  - docs/90_Design/API_Validation_v1.md — 엔드포인트별 필드 검증/오류/성능 기준
  - docs/90_Design/ErrorCodes.md — 에러 코드 사전(코드→HTTP→설명)
  - docs/90_Design/Perf_Test.md — 성능 측정 계획(JMeter/EXPLAIN, SLA)
  - docs/90_Design/Sec_Scan_Runbook.md — 취약점 점검 절차(의존성/정적분석)
  - docs/90_Design/Seeds_KeyMap.md — 시드 ID/코드 맵(샘플 키 한눈에)
- 환경 세팅 가이드(Windows)
  - SETUP_WINDOWS.md — Windows 개발 환경 세팅 상세 가이드(오류 해결 포함)
  - tools/setup_dev_env.ps1 — 통합 셋업 스크립트(DB 도커, 프론트 자동 TS 스캐폴드, 백엔드 로컬 실행)
  - tools/db_lint.py — 배포 전 DB 품질 자동 검증(무결성·성능·일관성)과 리포트 생성(50_QA->db_lint_report.md)
  - sql/TestDBSeed.sql — 16가지 시나리오 테스트용 seed DB
  - sql/TestDB_Transactions.sql - 16가지 시나리오 테스트용 Transactions
    - 주요 옵션:
      - -BackendDir / -FrontendDir — 백/프 경로 자동/지정
      - -MysqlPort / -AutoPort — DB 포트 지정/자동 회피
      - -SeedFile — 시드 SQL 경로 지정(있을 때만 주입)
      - -StrictVersion — Java<17/Node<18이면 중단
      - -NonInteractive — 사용자 입력 없이 실행


## 빠른 시작(Windows)
1) PowerShell 권한 설정
```
Set-ExecutionPolicy RemoteSigned
```
2) 통합 셋업 실행
```
.\setup_dev_env.ps1
```
- DB: Docker로 MySQL 컨테이너 기동
- Backend: application-local.yml 자동 생성
- Frontend: package.json 없으면 react-ts + vite 5 자동 스캐폴딩
- 포트 충돌 시 -AutoPort 또는 -MysqlPort로 대체 포트 사용

3) 개발 서버 접속
- API: http://localhost:8080
- Web: http://localhost:3000
- DB: mysql://mes_user:mes_pwd@localhost:3306/globalmed

자세한 옵션/오류 해결: SETUP_WINDOWS.md 참고

## 팀 규칙(협업)
- 버전 핀
  - Node 18.x, Java 17 LTS, Vite 5.4.x, Spring Boot 3.2.x, MySQL 8.0.x
- 브랜치
  - main: 안정 릴리스
  - develop: 통합
  - feature/xxx, fix/xxx, docs/xxx
- 커밋 메시지
  - feat: …, fix: …, docs: …, chore: …, test: …
- PR 체크리스트
  - db_lint critical=0
  - 스모크 통과(T01~T16 중 바뀐 영역)
  - API 에러 포맷 표준 준수 
  - 성능 기준 영향 시 노트 첨부

## 라이선스/문의
- 내부 프로젝트 사용. 문의: 팀 리드/PM.
> 자세한 설정/문제 해결은 SETUP_WINDOWS.md 참고