# M0. 계약·환경 락(반나절)
- 산출물
OpenAPI v0(6종): 계획/품목·UoM·BOM 증분, 지시 생성/전이, 실적, Backflush
공통정책 문구: UTC ISO, P/R/C, Idempotency 헤더, 기간 31일/페이지 100, UoM 18,6 half-up
플래그: Shadow 전역 ON, 엔드포인트별(지시·실적 OFF 후보 / Backflush·Cost ON)
- DoD
스펙 파일 freeze, 환경키(ERP_BASE_URL, ERP_API_KEY, Shadow 토글) 배치
- Gate
스펙 변경 No. 모든 팀 합의 OK이면 M1 진입

# M1. ERP BE(Java) 스켈레톤(1일)
- 산출물
Spring Boot 서비스: API Key 필터, Idempotency 인터셉터, 표준 에러 핸들러
6 엔드포인트 Stub + Swagger UI
- DoD
모든 엔드포인트 200/201/202 기본 응답, 헤더 기반 멱등 재생 확인
- Gate
Swagger 열람 OK, 헤더 멱등(POST 2회 동일 바디) 확인되면 M2

# M2. ERP→MES 증분 제공(계획·BOM·품목)(1일)
- 산출물
GET /items?updatedSince, GET /boms?updatedSince, POST/PUT /plans
BOM 헤더/라인·UoM 필드/유효기간·삭제정책 반영
- DoD
updatedSince 증분·페이지 상한·UTC 검증 통과, 샘플 데이터로 Backflush 산식 검증 OK
- Gate
MES 캐시 갱신 테스트(샘플) 통과 시 M3

# M3. MES→ERP 라이브 후보(지시/전이/실적) 연결(1일)
- 산출물
ERP BE 실제 처리(지시 생성·전이, 실적 저장) + 멱등 hit 재생 표식 헤더(X-Idempotency-Replayed)
MES Outbox 워커 → ERP 호출(지시/전이/실적만 Shadow OFF)
- DoD
지시/전이/실적 왕복 스모크 ALL PASS(멱등 포함), 에러 포맷·타임아웃·재시도 정상
- Gate
Outbox RETRY/FAILED 0, validation fail <0.5%면 M4

# M4. Backflush·Cost Shadow 운용 고정(반나절)
- 산출물
Backflush: ERP 수신/검증만(Shadow ON), 에러 코드(BOM/UoM/유효기간) 정리
Cost: Shadow 전용 payload 로깅(EstCost는 MES 화면만)
- DoD
Backflush 202 + wouldSend 로깅 누적, 대사 리포트에 Shadow 통계 포함
- Gate
Shadow 로깅/대사 지표 확인되면 M5

# M5. ERP 콘솔 FE(C# Razor) 1차(1일)
- 산출물
대시보드 카드(SENT/RETRY/FAILED/SHADOW), Outbox 리스트(재전송), Shadow 토글, 지시/전이·실적 폼
- DoD
전역/엔드포인트 Shadow 배지 표시, 재전송(지시/실적만) 동작
- Gate
관제에서 전환/재전송 테스트 OK면 M6
# M6. 대사·알림·전환(반나절)

- 산출물
일일 02:00 UTC 대사(지시/실적/소모 합계) + 알림
전환 기준표: validation 실패율 <0.5%, RETRY/FAILED 0, 대사 불일치 0 → 지시/실적 Live 유지, Backflush/Cost Shadow 유지
- DoD
첫 리포트 생성·알림 수신 확인
- Gate
기준 충족 시 “시연 가능” 상태 확정

# M7. 품질 보강(선택, 1일)
- 산출물
Swagger 전역 헤더(X-API-Key, X-Idempotency-Key)
응답 헤더 X-Idempotency-Replayed 표식(모든 엔드포인트)
캐시 키 범위 확장(method:path:key)
- DoD
Swagger에서도 멱등 재생 식별 가능, 키 충돌 없음
—

## 하루 실행 루틴(요약)
Day 1: M1
Day 2: M2
Day 3: M3
Day 4: M4 + M5(절반 병행)
Day 5: M5 마감 + M6
(+1d 버퍼로 M7 품질 보강)
## 스모크 체크(매일 끝에)
지시 생성 201 → 같은 키 재전송 동일 201(바디 동일)
전이 200 → 같은 키 재전송 동일 200
실적 201 → 같은 키 재전송 동일 201
Backflush 202(Shadow) → 대사 집계 반영
오류 케이스: UOM_MAPPING_MISSING, BOM_NOT_EFFECTIVE, TIME_ORDER_INVALID, DUPLICATE_KEY 확인