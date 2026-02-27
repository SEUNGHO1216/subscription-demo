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
}