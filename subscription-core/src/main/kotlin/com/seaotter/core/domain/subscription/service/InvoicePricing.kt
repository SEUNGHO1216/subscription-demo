package com.seaotter.core.domain.subscription.service

import com.seaotter.core.domain.subscription.BillingStrategy
import com.seaotter.core.domain.subscription.Subscription
import java.math.RoundingMode
import java.time.temporal.ChronoUnit

/**
 * 정기결제 스케줄러가 만드는 송장의 청구 금액 계산.
 * 외부 상태(현재 시각, DB)에 기대지 않는 순수 함수라 단위 테스트로 금액을 고정해 검증한다.
 */
object InvoicePricing {

  /**
   * 결제 기준일(anchorPayDays)을 둔 후불 구독의 첫 결제는 가입일부터 결제일까지 일할 계산한다.
   * 그 외에는 요금제 기본 가격을 그대로 청구한다.
   */
  fun priceOf(subscription: Subscription): Long {
    val snapshot = subscription.planSnapshot
    if (
      subscription.payVersion == 0L &&
      snapshot.billingPolicy.billingStrategy == BillingStrategy.POSTPAID &&
      !snapshot.billingPolicy.anchorPayDays.isNullOrEmpty()
    ) {
      return proratedPrice(subscription)
    }
    return snapshot.billingPolicy.convertToLongTypePrice(snapshot.priceInfo.defaultPrice)
  }

  // 기본 가격 ÷ 한 주기 일수 × 이용 일수. 나눗셈은 소수 10자리까지 유지하고 마지막에 요금제의 반올림 방식으로 정수화한다.
  private fun proratedPrice(subscription: Subscription): Long {
    val nextBillingAt = requireNotNull(subscription.nextBillingAt) { "다음 결제일이 없는 구독은 일할 계산을 할 수 없습니다." }
    val useDays = ChronoUnit.DAYS.between(subscription.createdAt.toLocalDate(), nextBillingAt.toLocalDate())

    val billingPolicy = subscription.planSnapshot.billingPolicy
    val totalTermDays = billingPolicy.countDays(nextBillingAt)

    return billingPolicy.convertToLongTypePrice(
      subscription.planSnapshot.priceInfo.defaultPrice
        .divide(totalTermDays.toBigDecimal(), 10, RoundingMode.HALF_UP)
        .multiply(useDays.toBigDecimal())
    )
  }
}
