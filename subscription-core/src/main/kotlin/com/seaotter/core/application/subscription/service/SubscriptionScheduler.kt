package com.seaotter.core.application.subscription.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SubscriptionScheduler(
  private val subscriptionCommandService: SubscriptionCommandService
) {

  val log = LoggerFactory.getLogger(SubscriptionScheduler::class.java)

//  @Scheduled(cron = "0 */2 * * * *") // 테스트 3분
  @Scheduled(cron = "0 0 0 * * *") // 운영은 자정
  fun generateInvoicePeriodically() {
    log.info("Generating invoice periodically start! ")
    val start = System.currentTimeMillis()
    subscriptionCommandService.generateInvoice(date = LocalDate.now())
    log.info("Generating invoice periodically end! 소요시간 : {}", System.currentTimeMillis() - start)
  }

//  @Scheduled(cron = "0 * * * * *") // 테스트 매분
  @Scheduled(cron = "0 0 0 * * *") // 운영은 자정
  fun terminateCompletePeriodically() {
    subscriptionCommandService.terminateCompleteSubscription(date = LocalDate.now())
  }

//  @Scheduled(cron = "30 */2 * * * *") // 테스트 3분
  @Scheduled(cron = "0 0 17 * * *")
  fun paySubscriptionPeriodically() {
    log.info("Paying subscription periodically start!")
    val start = System.currentTimeMillis()
    subscriptionCommandService.integratedBillingSubscription(date = LocalDate.now())
    log.info("Paying subscription periodically end! 소요시간 : {}", System.currentTimeMillis() - start)
  }
}