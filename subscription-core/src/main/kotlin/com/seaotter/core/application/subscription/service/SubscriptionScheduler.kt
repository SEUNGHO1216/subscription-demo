package com.seaotter.core.application.subscription.service

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 서버가 여러 대여도 각 스케줄은 한 대에서만 실행한다(@SchedulerLock).
 *  - 같은 시각에 cron 이 울려도 shedlock 테이블의 락을 먼저 얻은 서버만 실행하고, 나머지는 기다리지 않고 건너뛴다.
 *  - lockAtLeastFor: 작업이 금방 끝나도 이 시간 동안은 락을 쥐고 있어, 시계가 몇 초 늦은 서버가 같은 회차를 다시 실행하지 못하게 한다.
 *  - lockAtMostFor: 실행 중 서버가 죽어도 이 시간이 지나면 락이 풀린다. 작업의 최대 소요 시간보다 넉넉히 잡는다.
 * 락이 만료돼 겹치거나 수동 실행과 겹치는 드문 경우는 엔티티의 낙관적 락(@Version)과 송장 유니크 제약이 막는다.
 */
@Component
class SubscriptionScheduler(
  private val subscriptionCommandService: SubscriptionCommandService
) {

  val log = LoggerFactory.getLogger(SubscriptionScheduler::class.java)

//  @Scheduled(cron = "0 */2 * * * *") // 테스트 3분
  @Scheduled(cron = "0 0 0 * * *") // 운영은 자정
  @SchedulerLock(name = "generate-invoice", lockAtLeastFor = "PT1M", lockAtMostFor = "PT30M")
  fun generateInvoicePeriodically() {
    log.info("Generating invoice periodically start! ")
    val start = System.currentTimeMillis()
    subscriptionCommandService.generateInvoice(date = LocalDate.now())
    log.info("Generating invoice periodically end! 소요시간 : {}", System.currentTimeMillis() - start)
  }

//  @Scheduled(cron = "0 * * * * *") // 테스트 매분
  @Scheduled(cron = "0 0 0 * * *") // 운영은 자정
  @SchedulerLock(name = "terminate-complete", lockAtLeastFor = "PT1M", lockAtMostFor = "PT30M")
  fun terminateCompletePeriodically() {
    subscriptionCommandService.terminateCompleteSubscription(date = LocalDate.now())
  }

//  @Scheduled(cron = "30 */2 * * * *") // 테스트 3분
  @Scheduled(cron = "0 0 17 * * *")
  // PG 호출이 들어가 송장 수와 PG 응답 속도에 따라 길어질 수 있으므로 넉넉히 잡는다
  @SchedulerLock(name = "pay-subscription", lockAtLeastFor = "PT1M", lockAtMostFor = "PT2H")
  fun paySubscriptionPeriodically() {
    log.info("Paying subscription periodically start!")
    val start = System.currentTimeMillis()
    subscriptionCommandService.integratedBillingSubscription(date = LocalDate.now())
    log.info("Paying subscription periodically end! 소요시간 : {}", System.currentTimeMillis() - start)
  }
}
