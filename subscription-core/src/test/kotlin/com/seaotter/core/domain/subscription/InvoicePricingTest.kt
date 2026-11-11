package com.seaotter.core.domain.subscription

import com.seaotter.core.domain.subscription.SubscriptionFixture.monthlyPolicy
import com.seaotter.core.domain.subscription.SubscriptionFixture.snapshot
import com.seaotter.core.domain.subscription.SubscriptionFixture.subscription
import com.seaotter.core.domain.subscription.service.InvoicePricing
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.RoundingMode
import java.time.LocalDateTime
import kotlin.test.assertEquals

class InvoicePricingTest {

  // 1/11 가입, 기준일 1일 → 첫 결제일 2/1. 한 주기는 1/1 ~ 2/1 = 31일, 이용 일수는 1/11 ~ 2/1 = 21일
  private val createdAt = LocalDateTime.of(2026, 1, 11, 10, 0)
  private val firstBillingAt = LocalDateTime.of(2026, 2, 1, 10, 0)

  @Test
  fun `기준일이 있는 후불 구독의 첫 결제는 이용한 날만큼 일할 계산한다`() {
    val sub = subscription(
      snapshot = snapshot(price = 31_000, policy = monthlyPolicy(BillingStrategy.POSTPAID, anchorPayDay = 1)),
      createdAt = createdAt,
      nextBillingAt = firstBillingAt,
    )

    assertEquals(21_000, InvoicePricing.priceOf(sub))
  }

  @ParameterizedTest(name = "{0} → {1}원")
  @CsvSource("HALF_UP, 6774", "FLOOR, 6774", "CEILING, 6775")
  fun `일할 금액은 요금제의 반올림 방식으로 원 단위를 정한다`(mode: RoundingMode, expected: Long) {
    // 10,000 ÷ 31 × 21 = 6,774.19...
    val sub = subscription(
      snapshot = snapshot(price = 10_000, policy = monthlyPolicy(BillingStrategy.POSTPAID, anchorPayDay = 1, roundMode = mode)),
      createdAt = createdAt,
      nextBillingAt = firstBillingAt,
    )

    assertEquals(expected, InvoicePricing.priceOf(sub))
  }

  @Test
  fun `두 번째 결제부터는 기본 가격을 청구한다`() {
    val sub = subscription(
      snapshot = snapshot(price = 31_000, policy = monthlyPolicy(BillingStrategy.POSTPAID, anchorPayDay = 1)),
      createdAt = createdAt,
      nextBillingAt = LocalDateTime.of(2026, 3, 1, 10, 0),
      payVersion = 1,
    )

    assertEquals(31_000, InvoicePricing.priceOf(sub))
  }

  @Test
  fun `선불 구독의 정기 송장은 기본 가격을 청구한다`() {
    val sub = subscription(
      snapshot = snapshot(price = 31_000, policy = monthlyPolicy(BillingStrategy.PREPAID, anchorPayDay = 1)),
      createdAt = createdAt,
      nextBillingAt = firstBillingAt,
    )

    assertEquals(31_000, InvoicePricing.priceOf(sub))
  }
}
