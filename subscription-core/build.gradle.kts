dependencies {
  implementation("com.github.f4b6a3:ulid-creator:5.2.3")
  implementation("org.springframework:spring-tx:6.2.16")
  // 스케줄러를 서버 한 대에서만 실행하기 위한 어노테이션(@SchedulerLock)
  implementation("net.javacrumbs.shedlock:shedlock-spring:6.3.1")
}
