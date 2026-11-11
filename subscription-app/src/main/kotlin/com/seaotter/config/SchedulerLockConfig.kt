package com.seaotter.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

/**
 * 스케줄러 락을 모든 서버가 함께 쓰는 DB(shedlock 테이블)에 둔다.
 * 락 획득은 "만료된 락만 가져간다"는 조건부 UPDATE 한 문장이라, 동시에 시도해도 한 서버만 성공한다.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
class SchedulerLockConfig {

  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider =
    JdbcTemplateLockProvider(
      JdbcTemplateLockProvider.Configuration.builder()
        .withJdbcTemplate(JdbcTemplate(dataSource))
        // 만료 판단을 각 서버 시계가 아니라 DB 시계로 한다. 서버 간 시계 차이에 흔들리지 않는다.
        .usingDbTime()
        .build()
    )
}
