# Windows 개발 환경 세팅 가이드

이 문서는 “DB=Docker, 프론트/백=로컬” 하이브리드 개발 환경을 1회 셋업으로 끝내기 위한 가이드입니다.

## 사양/전제
- OS: Windows 10/11
- 필수
  - Docker Desktop + WSL2
  - Java 17(Temurin 권장)
  - Node 18.x + pnpm
  - PowerShell 실행 정책: RemoteSigned

## 1. 필수 설치
- Java 17
- Node 18 / vite 5.4.10 / pnpm
- Docker Desktop(WSL2 활성)

버전 확인
```
java -version
node -v
docker compose version
```

## 2. 통합 스크립트 실행
- 위치: tools/setup_dev_env.ps1
- 최초 권한
```
Set-ExecutionPolicy RemoteSigned
```
- 기본 실행(자동 경로)
```
.\setup_dev_env.ps1
```
- 경로/포트 지정 실행
```
.\setup_dev_env.ps1 -BackendDir "C:\dev\backend" -FrontendDir "C:\dev\frontend" -MysqlPort 3307
```
- 포트 자동 회피
```
.\setup_dev_env.ps1 -AutoPort
```
- 엄격 버전(미달 시 중단)
```
.\setup_dev_env.ps1 -StrictVersion
```

## 3. 스크립트가 하는 일
- 사전 체크
  - Docker/Compose/Java17/Node18 확인
  - Java/Node 미달 시 계속/중단 선택(Strict면 중단)
- 포트 점유 감지
  - 3306 점유 시 PID/이름 표시, -AutoPort면 자동 대체 포트
- 컨테이너 중복 방지
  - GlobalMedMES_db 이름 컨테이너가 있으면 자동 제거
- Docker DB 기동
  - compose.dev.yml 생성 → MySQL 컨테이너 up → Health 또는 Running/no-healthcheck 확인
- DB 초기화/시드
  - tools/sql/wipe_all.sql → tools/sql/dev_seed_plus_ordered.sql 주입(있을 때)
- Backend 환경
  - src/main/resources/application-local.yml 자동 생성
- Frontend 환경 및 자동 스캐폴딩
  - .env.local 생성(VITE_API_BASE)
  - package.json 없으면 react-ts + vite 5.4.19 + tailwind 자동 스캐폴딩
  - pnpm dev 실행(3000)
- 기동
  - 백엔드 gradlew 있으면 bootRun(local 프로필) 실행
  - 프론트 pnpm dev 실행

## 4. 자주 쓰는 옵션
- -BackendDir, -FrontendDir: 경로 지정(미지정 시 현재\backend / 현재\frontend 자동 생성)
- -MysqlPort: MySQL 호스트 포트(기본 3306)
- -AutoPort: 포트 점유 시 자동 대체
- -SeedFile: 시드 파일 경로 지정(기본 tools\sql\dev_seed_plus_ordered.sql)
- -StrictVersion: Java17/Node18 미만이면 즉시 중단
- -NonInteractive: 사용자 입력 없이 실행(경고는 통과/오류는 중단)

## 5. 실행 주소
- API: http://localhost:8080
- Web: http://localhost:3000
- DB: mysql://mes_user:mes_pwd@localhost:{선택포트}/globalmed

## 6. 개발 루틴
- DB 리셋
```
docker exec -i GlobalMedMES_db mysql -uroot -proot globalmed < tools/sql/wipe_all.sql
docker exec -i GlobalMedMES_db mysql -uroot -proot globalmed < tools/sql/dev_seed_plus_ordered.sql
```
- 백엔드
```
cd backend
.\gradlew bootRun -Dspring.profiles.active=local
# JAR 실행
.\gradlew bootJar
java -jar build\libs\*.jar --spring.profiles.active=local
```
- 프론트
```
cd frontend
pnpm i
pnpm dev -- --port 3000
```

## 7. 트러블슈팅(실제 대응 사례)

- 포트 3306 바인딩 실패
  - 원인: 기존 MySQL 서비스/컨테이너가 점유
  - 해결: `.\setup_dev_env.ps1 -AutoPort` 또는 서비스/컨테이너 종료 후 재실행

- java.exe NativeCommandError (java -version)
  - 원인: stderr 출력 처리 문제
  - 해결: 스크립트에서 cmd /c로 우회(이미 반영)

- PID 변수 충돌
  - 원인: $PID 자동 변수와 사용자 변수명 충돌
  - 해결: $procId 변수로 변경(이미 반영)

- docker health 템플릿 에러
  - 원인: HealthCheck가 없는 이미지
  - 해결: Health 없으면 .State.Status로 fall-back(이미 반영)

- 프론트 package.json 없음
  - 원인: 스캐폴딩 전 dev 실행
  - 해결: react-ts + vite 5.4.19 자동 스캐폴딩(동의 시)

- vite 7 + crypto.hash 에러
  - 원인: Node/OpenSSL 조합 이슈
  - 해결: vite 5.4.x로 다운핀(현재 5.4.19)

- npm ERR! could not determine executable
  - 원인: 스크립트가 패키지 없는 폴더에서 실행
  - 해결: 자동 스캐폴딩 후 pnpm i/dev로 해결

## 8. 로컬 DB로 개발하고 싶을 때
- Docker 종료
```
docker rm -f GlobalMedMES_db
```
- application-local.yml의 JDBC URL을 로컬 포트로 변경
```
jdbc:mysql://localhost:3306/globalmed?serverTimezone=UTC
```
- 동일 시드/린트 실행으로 팀 기준과 동기화

## 9. 체크리스트(새 멤버)
- Java 17 / Node 18 / Docker OK
- `.\setup_dev_env.ps1` 실행 OK
- 브라우저에서 http://localhost:3000 접속 OK
- Postman 9건 스모크(로그인→지시→전이→RUN→실적→KPI) 통과
- db_lint critical=0 유지