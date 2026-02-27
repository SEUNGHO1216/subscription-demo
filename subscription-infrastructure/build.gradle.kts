plugins {
  kotlin("plugin.jpa")
  kotlin("kapt")
  id("io.spring.dependency-management")
}

dependencies {
  implementation(project(":subscription-core"))
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
  runtimeOnly("com.mysql:mysql-connector-j")

  // QueryDSL (jakarta 필수)
  implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
  kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
}