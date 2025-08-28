Param(
  [string]$BackendDir = "",
  [string]$FrontendDir = "",
  [int]$ApiPort = 8080,
  [int]$WebPort = 3000,
  [int]$MysqlPort = 3306,
  [string]$DbName = "globalmed",
  [string]$DbUser = "mes_user",
  [string]$DbPass = "mes_pwd",
  [string]$DbRoot = "root",
  [string]$MysqlImage = "mysql:8.0.32",
  [string]$ComposeFile = "compose.dev.yml",
  [string]$AppName = "GlobalMedMES",
  [string]$ContainerName = "GlobalMedMES_db",
  [string]$SeedFile = "tools\sql\dev_seed_plus_ordered.sql", # 있으면 주입
  [string]$ViteVersion = "5.4.19",   # 자동 스캐폴딩 시 vite 고정 버전
  [switch]$AutoPort,                 # 3306 점유 시 자동 대체 포트 탐색
  [switch]$StrictVersion,            # Java<17 또는 Node<18이면 즉시 중단
  [switch]$NonInteractive            # 사용자 입력 프롬프트 없이 중단/진행
)

$ErrorActionPreference = "Stop"

# ---------- 컬러 로그 ----------
function Info($m){ Write-Host $m -ForegroundColor Cyan }
function Ok($m){ Write-Host $m -ForegroundColor Green }
function Warn($m){ Write-Host $m -ForegroundColor Yellow }
function Fail($m){ Write-Host $m -ForegroundColor Red }
function Require-Cmd($n){ if (-not (Get-Command $n -ErrorAction SilentlyContinue)) { Fail "$n 필요"; throw "$n not found" } }
function Ensure-Dir([string]$p){ if (-not (Test-Path $p)) { New-Item -ItemType Directory -Path $p -Force | Out-Null } }

# ---------- 포트 점유 검사 ----------
function Test-PortUse {
  param([int]$Port)
  try {
    $conns = Get-NetTCPConnection -LocalPort $Port -ErrorAction Stop
    if ($conns) {
      $detail = $conns | Select-Object -First 1 | ForEach-Object {
        $pId = $_.OwningProcess
        $proc = Get-Process -Id $pId -ErrorAction SilentlyContinue
        $procName = if ($proc) { $proc.ProcessName } else { "Unknown" }
        @{ PID = $pId; Name = $procName }
      }
      return @{ Used = $true; Detail = $detail }
    }
  } catch {
    $raw = netstat -ano | Select-String -Pattern "LISTENING.*[:.]$Port\s"
    if ($raw) {
      $pId = ($raw -split "\s+")[-1]
      $proc = Get-Process -Id $pId -ErrorAction SilentlyContinue
      $procName = if ($proc) { $proc.ProcessName } else { "Unknown" }
      return @{ Used = $true; Detail = @{ PID=$pId; Name=$procName } }
    }
  }
  return @{ Used = $false; Detail = $null }
}
function Find-FreePort([int]$Start){ $p=$Start; for($i=0;$i -lt 100;$i++){ $t=Test-PortUse -Port $p; if(-not $t.Used){return $p}; $p++ }; return $null }
function Remove-ContainerIfExists([string]$NamePart){
  $names = (docker ps -a --filter "name=$NamePart" --format "{{.Names}") 2>$null
  if ($names) {
    Warn "기존 컨테이너 감지: $names → 제거"
    $names -split "\r?\n" | ForEach-Object { if ($_ -ne "") { docker rm -f $_ | Out-Null } }
  }
}

# ---------- 1) 사전 체크 ----------
Info "==> [1/8] 사전 체크(Docker/Compose/Java17/Node18)"
Require-Cmd docker
docker compose version *>$null

# Java / Node 버전 체크 (안전)
$javaOk = $true; $nodeOk = $true
try {
  $javaOut = cmd /c "java -version 2>&1"
  if (-not $javaOut) { $javaOk = $false; Warn "Java 실행 실패 또는 미설치" }
  else {
    $javaVer = ($javaOut | Select-String -Pattern 'version\s+"(?<ver>[\d\.]+)"').Matches.Groups['ver'].Value
    if (-not $javaVer) { $javaOk = $false; Warn "Java 버전 파싱 실패: $javaOut" }
    else { if ([int]($javaVer.Split('.')[0]) -lt 17) { $javaOk = $false; Warn "Java 17 권장(현재: $javaVer)" } }
  }
} catch { $javaOk = $false; Warn "Java 확인 예외: $($_.Exception.Message)" }

try {
  $nodeRaw = (& node -v) 2>$null   # v18.17.1 등
  if (-not $nodeRaw) { $nodeOk = $false; Warn "Node 미설치/PATH 미등록" }
  else {
    $nodeVer = $nodeRaw.TrimStart('v')
    if ([int]($nodeVer.Split('.')[0]) -lt 18) { $nodeOk = $false; Warn "Node 18 권장(현재: v$nodeVer)" }
  }
} catch { $nodeOk = $false; Warn "Node 확인 예외: $($_.Exception.Message)" }

if (-not $javaOk -or -not $nodeOk) {
  if ($StrictVersion) { Fail "버전 조건 불만족 → 중단"; exit 1 }
  if (-not $NonInteractive) {
    $resp = Read-Host "버전이 권장 조건 미달입니다. 계속할까요? (Y/n)"
    if ($resp -and $resp.ToLower() -eq 'n') { Fail "사용자 중단 선택"; exit 1 }
  } else { Warn "NonInteractive: 버전 경고 무시하고 계속" }
}

# ---------- 2) 포트 점유 확인 ----------
Info "==> [2/8] 포트 점유 검사(MySQL $MysqlPort)"
$portInfo = Test-PortUse -Port $MysqlPort
if ($portInfo.Used) {
  $procId = $portInfo.Detail.PID; $procName = $portInfo.Detail.Name
  Fail "포트 $MysqlPort 점유중: PID=$procId, Name=$procName"
  if ($AutoPort) {
    $newPort = Find-FreePort -Start ($MysqlPort + 1)
    if (-not $newPort) { Fail "대체 포트 탐색 실패"; exit 1 }
    Warn "대체 포트 자동 선택 → $newPort"
    $MysqlPort = $newPort
  } else {
    if ($NonInteractive) { Fail "NonInteractive: 종료"; exit 1 }
    $new = Read-Host "대체 포트를 입력(예: 3307). 종료하려면 빈값"
    if ([string]::IsNullOrWhiteSpace($new)) { exit 1 }
    $MysqlPort = [int]$new
  }
}
Ok "MySQL 포트 확정: $MysqlPort"

# ---------- 3) 경로 처리 ----------
Info "==> [3/8] Backend/Frontend 경로 준비"
if (-not $BackendDir -or $BackendDir -eq "") { $BackendDir = Join-Path $PWD "backend" }
if (-not $FrontendDir -or $FrontendDir -eq "") { $FrontendDir = Join-Path $PWD "frontend" }
Ensure-Dir $BackendDir
Ensure-Dir $FrontendDir
Ok "BackendDir=$BackendDir"
Ok "FrontendDir=$FrontendDir"

# ---------- 4) compose 생성 + 컨테이너 클린업 ----------
Info "==> [4/8] compose.dev.yml 생성 및 컨테이너 클린업"
Remove-ContainerIfExists -NamePart $ContainerName

@"
services:
  db:
    image: ${MysqlImage}
    container_name: ${ContainerName}
    environment:
      - MYSQL_ROOT_PASSWORD=${DbRoot}
      - MYSQL_DATABASE=${DbName}
      - MYSQL_USER=${DbUser}
      - MYSQL_PASSWORD=${DbPass}
      - TZ=UTC
    command: ["--default-time-zone=+00:00","--character-set-server=utf8mb4","--collation-server=utf8mb4_0900_ai_ci"]
    ports: ["${MysqlPort}:3306"]
    healthcheck:
      test: ["CMD","mysqladmin","ping","-h","127.0.0.1","-p${DbRoot}"]
      interval: 10s
      timeout: 5s
      retries: 12
"@ | Out-File -Encoding UTF8 $ComposeFile
Ok "$ComposeFile 생성 완료"

# ---------- 5) DB 기동 + Health 대기 ----------
Info "==> [5/8] DB 기동 및 Health 대기"
docker compose -f $ComposeFile up -d db | Out-Null

$tries=0
while ($true) {
  $state = docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $ContainerName 2>$null
  if ($state -eq "healthy" -or $state -eq "running" -or $state -eq "no-healthcheck") { break }
  Start-Sleep -Seconds 2
  $tries++
  if ($tries -gt 90) { Fail "DB health timeout (상태: $state)"; exit 1 }
}
Ok "DB 상태: $state"

# ---------- 6) Wipe & Seed ----------
Info "==> [6/8] DB 초기화 및 시드 주입"
Ensure-Dir "tools\sql"
$wipe = "tools\sql\wipe_all.sql"
if (-not (Test-Path $wipe)) {
@"
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE TB_INT_INBOX; TRUNCATE TABLE TB_INT_OUTBOX; TRUNCATE TABLE TB_ERP_WO_LINK; TRUNCATE TABLE TB_ERP_PROD_ORDER;
TRUNCATE TABLE TB_EQP_TELEMETRY;
TRUNCATE TABLE TB_ROLE_MENU; TRUNCATE TABLE TB_MENU; TRUNCATE TABLE TB_USER_ROLE; TRUNCATE TABLE TB_ROLE; TRUNCATE TABLE TB_USER;
TRUNCATE TABLE TB_AUDIT_LOG;
TRUNCATE TABLE TB_INSPECTION_RESULT; TRUNCATE TABLE TB_NON_CONFORMANCE; TRUNCATE TABLE TB_DEFECT; TRUNCATE TABLE TB_INSPECTION;
TRUNCATE TABLE TB_PRODUCTION_PERFORMANCE; TRUNCATE TABLE TB_EQUIPMENT_STATUS_LOG;
TRUNCATE TABLE TB_SHIFT_ASSIGNMENT; TRUNCATE TABLE TB_SHIFT_CALENDAR; TRUNCATE TABLE TB_SHIFT;
TRUNCATE TABLE TB_MATERIAL_LOT; TRUNCATE TABLE TB_BOM;
TRUNCATE TABLE TB_WORK_ORDER; TRUNCATE TABLE TB_PRODUCTION_PLAN;
TRUNCATE TABLE TB_EQUIPMENT; TRUNCATE TABLE TB_WAREHOUSE; TRUNCATE TABLE TB_ITEM; TRUNCATE TABLE TB_PROCESS; TRUNCATE TABLE TB_WORKCENTER; TRUNCATE TABLE TB_WORKSHOP;
TRUNCATE TABLE TB_KPI_TARGET;
TRUNCATE TABLE TB_CODE; TRUNCATE TABLE TB_CODE_GROUP;
SET FOREIGN_KEY_CHECKS = 1;
"@ | Out-File -Encoding UTF8 $wipe
}
# 주입
Get-Content $wipe | docker exec -i $ContainerName mysql -uroot -p$DbRoot $DbName
if (Test-Path $SeedFile) {
  Info "시드 주입: $SeedFile"
  Get-Content $SeedFile | docker exec -i $ContainerName mysql -uroot -p$DbRoot $DbName
} else {
  Warn "시드 파일 없음: $SeedFile → 주입 생략(개발만 먼저 가능)"
}
Ok "DB 초기화/시드 완료"

# ---------- 7) Backend/Frontend 환경 & 자동 TS 스캐폴딩(vite 5 핀) ----------
Info "==> [7/8] Backend/Frontend 환경 반영 및 프론트 자동 TS 스캐폴딩"

# Backend local 프로필
$appLocal = Join-Path $BackendDir "src\main\resources\application-local.yml"
Ensure-Dir (Split-Path $appLocal)
@"
server:
  port: ${ApiPort}
spring:
  datasource:
    url: jdbc:mysql://localhost:${MysqlPort}/${DbName}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
    username: ${DbUser}
    password: ${DbPass}
  jpa:
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
logging:
  level:
    root: INFO
"@ | Out-File -Encoding UTF8 $appLocal
Ok "생성/갱신: $appLocal"

# Frontend .env.local
$feEnv = Join-Path $FrontendDir ".env.local"
"VITE_API_BASE=http://localhost:${ApiPort}" | Out-File -Encoding UTF8 $feEnv
Ok "생성/갱신: $feEnv"

# 프론트 자동 스캐폴딩 (TS + Tailwind + vite 핀)
$pkgJson = Join-Path $FrontendDir "package.json"
if (-not (Test-Path $pkgJson)) {
  if (-not $NonInteractive) {
    $resp = Read-Host "frontend에 package.json이 없습니다. React+Vite(react-ts, vite@$ViteVersion)로 자동 생성할까요? (Y/n)"
    if ($resp -and $resp.ToLower() -eq 'n') { Warn "스캐폴드 생략"; goto FE_DONE }
  }
  if (!(Get-Command pnpm -ErrorAction SilentlyContinue)) { npm i -g pnpm }
  Push-Location $FrontendDir
  pnpm dlx create-vite@latest . -- --template react-ts
  pnpm i
  pnpm add -D vite@$ViteVersion @vitejs/plugin-react@4.3.1 typescript@5.4.x @types/react@18.2.x @types/react-dom@18.2.x
  pnpm add -D tailwindcss@3.4 postcss autoprefixer
  npx tailwindcss init -p
  # Tailwind content 고정
  (Get-Content "tailwind.config.js") -replace 'content:\s*\[[^\]]*\]',
    'content: ["./index.html","./src/**/*.{js,ts,jsx,tsx}"]' | Set-Content "tailwind.config.js"
  # Tailwind 지시어 보장
  if (-not (Test-Path ".\src\index.css")) { New-Item -ItemType File -Path ".\src\index.css" -Force | Out-Null }
  $css = Get-Content ".\src\index.css" -Raw
  if ($css -notmatch "@tailwind base") {
    "@tailwind base;`n@tailwind components;`n@tailwind utilities;`n`n$css" | Set-Content ".\src\index.css"
  }
  # package.json의 vite 버전을 정확핀(캐럿 제거)
  $pj = Get-Content "package.json" -Raw | ConvertFrom-Json
  if ($pj.devDependencies.vite) { $pj.devDependencies.vite = "$ViteVersion" }
  if ($pj.devDependencies."@vitejs/plugin-react") { $pj.devDependencies."@vitejs/plugin-react" = "4.3.1" }
  $pj | ConvertTo-Json -Depth 10 | Out-File -Encoding UTF8 "package.json"
  Pop-Location
  Ok "Frontend 템플릿 자동 생성 완료(vite @$ViteVersion, react-ts)"
}

:FE_DONE
# 프론트 dev 실행 (package.json 있을 때만)
if (Test-Path $pkgJson) {
  Start-Process powershell -ArgumentList "-NoExit","-Command","cd `"$FrontendDir`"; if (!(Get-Command pnpm -ErrorAction SilentlyContinue)) { npm i -g pnpm }; pnpm i; pnpm dev -- --port ${WebPort}"
  Ok "Frontend pnpm dev 시작 (port ${WebPort})"
} else {
  Warn "package.json 미존재 → 프론트 dev 실행 생략"
}

# 백엔드 gradlew 감지되면 bootRun
if (Test-Path (Join-Path $BackendDir "gradlew")) {
  Start-Process powershell -ArgumentList "-NoExit","-Command","cd `"$BackendDir`"; ./gradlew bootRun -Dspring.profiles.active=local"
  Ok "Backend bootRun 시작"
} else {
  Warn "BackendDir에 gradlew 미검출 → 수동 기동 필요"
}

# ---------- 8) 요약 ----------
Ok "`n✅ 개발 환경 준비 완료"
Write-Host "DB:   mysql://${DbUser}:${DbPass}@localhost:${MysqlPort}/${DbName}"
Write-Host "API:  http://localhost:${ApiPort}"
Write-Host "WEB:  http://localhost:${WebPort}"
Warn "[주의] 시드 파일이 있을 경우 TB_USER.password_hash는 실제 bcrypt 해시로 교체 권장"