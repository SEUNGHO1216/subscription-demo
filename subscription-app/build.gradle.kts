import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("io.spring.dependency-management")
}

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = true
jar.enabled = false

dependencies {
  implementation(project(":subscription-core"))
  implementation(project(":subscription-infrastructure"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  // 스케줄러 중복 실행 방지: 공유 DB 의 shedlock 테이블로 한 서버만 실행권을 얻는다
  implementation("net.javacrumbs.shedlock:shedlock-spring:6.3.1")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.3.1")

  // 동시성 통합 테스트: 실제 MySQL 에서 유니크 제약과 낙관적 락이 동작하는지 확인한다
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.testcontainers:mysql")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.springframework:spring-tx") // 예외 타입(DataIntegrityViolation 등) 확인용
}