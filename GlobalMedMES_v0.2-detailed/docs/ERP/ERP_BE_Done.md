# ERP MVP Backend 문서 (Spring Boot 3.5.4, Java 17, MySQL 8)

작성일: 2025-09-15  
작성자: 팀 ERP · 담당: 형

## 1. 개요
- 목표: BOM · 생산계획 · 원가 계산 MVP 백엔드
- 기술스택
  - Java 17, Spring Boot 3.5.4
  - Spring Web, Validation, Spring Data JPA
  - MySQL 8 (포트 3308), JDBC Driver
  - Swagger(springdoc-openapi) 2.7.0
  - Lombok, MapStruct(선택적)
- 운영 기본 정책
  - 인증/권한: MVP 무인증
  - 통화: 원화(KRW), 금액 최종 2자리 표시, 내부계산 6자리 유지
  - 수량: 소수점 6자리
  - 시간: 서버 UTC, 클라이언트 KST
  - 삭제: 논리삭제(is_deleted)
  - Open-Session-In-View: false (항상 DTO 응답)
  - 감사로그: MVP에선 미도입
  - 낙관적 락: version 컬럼

## 2. 주요 기능
- 제품(Product) 마스터 CRUD(현재 샘플: 생성)
- 표준단가(MaterialCost) 등록 및 기준일 조회
- BOM Header/Line 관리
  - 라인 조회(fetch join + DTO)
  - 라인 CUD(추가/삭제) 제공
- 생산계획(ProductionPlan)
  - 생성 시 유효·활성 BOM 검증
  - 상태 전이 API(DRAFT → CONFIRMED → IN_PRODUCTION → COMPLETED, CANCELED)
- 원가 계산
  - 제품 기준(by-product)
  - 생산계획 기준(by-plan)
  - 계산 결과 스냅샷 + 상세 저장

## 3. 환경 설정
- 서버 포트: 8081
- DB: localhost:3308, DB명 erp_mvp
- Swagger UI: /swagger-ui/index.html
- API Docs: /v3/api-docs

### 3.1 Gradle 설정 요약
- Spring Boot 3.5.4 / springdoc 2.7.0
- jackson-datatype-hibernate5 제거(직렬화 충돌 방지)
- javax.* 의존성 제거, DTO 응답 원칙

## 4. 스키마 개요
- UUID(CHAR(36))를 PK로 사용: tb_product, tb_material_cost, tb_bom_header, tb_bom_line, tb_production_plan, tb_cost_snapshot
- 스냅샷 상세는 BIGINT AI
- 유효기간: effective_from / effective_to
- BOM 유니크: (bom_id, parent_line_id, component_product_id)

## 5. 주요 설계 원칙
- DTO로만 응답(엔티티 직렬화 금지)
- 서비스 계층에서 fetch join 또는 별도 조회로 LAZY 안전 확보
- javax → jakarta 전환 일관성(의존성 충돌 방지)
- 소수점 처리: 내부 계산 6자리 고정, 출력 포맷은 FE에서 최종 2자리 권장

## 6. 엔드포인트 요약
- 제품
  - POST /api/products
- 표준단가
  - POST /api/material-costs
  - GET /api/material-costs/effective?productId=...&baseDate=...
- BOM
  - POST /api/boms
  - GET /api/boms/{bomId}/lines
  - POST /api/boms/lines
  - DELETE /api/boms/lines/{lineId}
- 생산계획
  - POST /api/plans
  - PATCH /api/plans/{planId}/status?nextStatus=...
- 원가
  - GET /api/costs/by-product/{productId}?qty=...&laborRate=...&overheadRate=...&baseDate=...
  - GET /api/costs/by-plan/{planId}?laborRate=...&overheadRate=...

## 7. Swagger/직렬화 이슈 해결 기록
- 원인: javax.* 심볼이 클래스패스에 남아 Jackson/Hibernate 직렬화 과정에서 충돌
- 조치:
  - jackson-datatype-hibernate5, JacksonConfig 제거
  - DTO 응답으로 일원화
  - build 의존성 정리, springdoc 2.7.0 유지
- 결과: /v3/api-docs, by-product, by-plan 모두 정상(테스트 완료)

## 8. 배포 전 체크리스트
- application.yml DB 접속, 포트 8081 확인
- 스키마/시드 적용 완료
- Swagger UI 정상 접속
- .http 스크립트 시나리오 1회 통과

## 9. 테스트 시나리오(HTTP Client .http)
- VS Code REST Client / IntelliJ HTTP Client 사용

[api-smoke.http]
- 제품 생성 → 단가 등록 → BOM 헤더/라인 → 계획 생성/전이 → 원가 계산 순서

## 10. 다음 단계(확장 없이)
- Flyway 도입(V1__init.sql로 스키마 고정)
- 입력 검증 강화(Validation 메시지)
- 로그 레벨 정리 및 마스킹

---
## 원가 계산 정책(옵션 C)
- Preview(미리보기): GET
  - /api/costs/by-product/{productId}
  - /api/costs/by-plan/{planId}
  - DB 저장 없음. 응답은 계산 결과만 반환
- Save(저장): POST
  - /api/costs/snapshots/by-product
  - /api/costs/snapshots/by-plan
  - 요청 바디의 조건으로 계산 후 스냅샷/상세를 DB에 기록
- 설계 노트
  - 미리보기 경로는 엔티티 직렬화 금지. 계산 시점에 필요한 문자열/ID 복제 후 DTO 변환
  - 저장 경로의 연관 주입은 getReferenceById/findById만 사용
  - Open-Session-In-View=false 유지
  
# 부록 A. Gradle 설정

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.4'
    id 'io.spring.dependency-management' version '1.1.6'
}

group = 'com.factory_dynamics.erp'
version = '0.0.1-SNAPSHOT'

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

repositories { mavenCentral() }

ext {
    springDocVersion = '2.7.0'
    mapstructVersion = '1.5.5.Final'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    runtimeOnly 'com.mysql:mysql-connector-j:8.4.0'
    implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocVersion}"

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.withType(Test).configureEach { useJUnitPlatform() }