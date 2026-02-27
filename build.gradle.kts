import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25" apply false
  kotlin("plugin.jpa") version "1.9.25" apply false
  kotlin("kapt") version "1.9.25" apply false
  id("org.springframework.boot") version "3.5.11" apply false
  id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {

  group = "com.seaotter"
  version = "0.0.1-SNAPSHOT"

  tasks.withType<Test> {
    useJUnitPlatform()
  }

  repositories {
    mavenCentral()
  }

}


subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "org.jetbrains.kotlin.plugin.spring") // allopen 처리 등
  apply(plugin = "org.springframework.boot") // bootJar task 생성, component scan 영향 등 => executable app 에만 사용하자
  apply(plugin = "io.spring.dependency-management")

  kotlin {
    compilerOptions {
      freeCompilerArgs.addAll("-Xjsr305=strict")
    }
    jvmToolchain(21)
  }

  val jar: Jar by tasks
  val bootJar: BootJar by tasks

  bootJar.enabled = false
  jar.enabled = true

  dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.0.1"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  }
}






