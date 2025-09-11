plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.factory_dynamics.erp" // 프로젝트명 수정
version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

configurations {
	compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories { mavenCentral() }

dependencies {
	// Spring Initializr에서 추가한 기본 의존성들
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security") // 보안
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// API 문서화
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

	// 기타 유틸리티 및 JWT, Captcha
	implementation("org.apache.commons:commons-lang3:3.18.0")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
	implementation("com.github.whvcse:easy-captcha:1.6.2")

	// Actuator
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// DB 드라이버
	runtimeOnly("com.mysql:mysql-connector-j")

	// 롬복
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// 테스트
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
	enabled = false // 테스트 비활성화
}