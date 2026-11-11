package com.seaotter.core.domain.subscription

import com.seaotter.core.domain.subscription.SubscriptionFixture.monthlyPolicy
import com.seaotter.core.domain.subscription.SubscriptionFixture.snapshot
import com.seaotter.core.domain.subscription.SubscriptionFixture.subscription
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubscriptionTest {

  @Nested
  inner class 활성_전환 {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(
      "PENDING, ACTIVE",
      "INACTIVE, ACTIVE",
      "ACTIVE, INACTIVE",
      "PAYMENT_ON_FAILURE, INACTIVE",
    )
    fun `상태에 따라 활성과 비활성을 오간다`(before: SubscriptionStatus, after: SubscriptionStatus) {
      val sub = subscription(status = before)

      sub.toggleActive()

      assertEquals(after, sub.status)
    }

    @ParameterizedTest
    @CsvSource("TERMINATE_READY", "TERMINATED")
    fun `해지 흐름 중인 구독은 전환할 수 없다`(status: SubscriptionStatus) {
      assertThrows<IllegalStateException> { subscription(status = status).toggleActive() }
    }
  }

  @Nested
  inner class 결제_기준일로_다음_결제일_정하기 {

    @ParameterizedTest(name = "가입 {0}, 기준일 {1}일 → {2}")
    @CsvSource(
      // 기준일이 이번 달에 아직 안 지났으면 이번 달
      "2026-03-05T10:00, 10, 2026-03-10T10:00",
      "2026-01-15T10:00, 31, 2026-01-31T10:00",
      // 기준일이 없는 달은 그 달의 말일
      "2026-02-10T10:00, 31, 2026-02-28T10:00",
      // 기준일 당일이나 이후에 가입하면 다음 달 (31일 기준일 → 2월은 말일)
      "2026-01-31T10:00, 31, 2026-02-28T10:00",
      // 12월 가입은 다음 해 1월 (이전 구현은 같은 해 1월, 즉 과거 날짜가 됐다)
      "2026-12-15T10:00, 10, 2027-01-10T10:00",
    )
    fun `다음 결제일을 계산한다`(commandAt: LocalDateTime, payDay: Int, expected: LocalDateTime) {
      val sub = subscription(snapshot = snapshot(policy = monthlyPolicy(anchorPayDay = payDay)))

      sub.initNextBillingAt(commandAt)

      assertEquals(expected, sub.nextBillingAt)
    }

    @Test
    fun `결제 후 다음 결제일이 짧은 달을 지나도 기준일로 돌아온다`() {
      val sub = subscription(
        snapshot = snapshot(policy = monthlyPolicy(anchorPayDay = 31)),
        nextBillingAt = LocalDateTime.of(2026, 1, 31, 10, 0),
      )

      val dates = (1..3).map { sub.updateNextBillingAt(); sub.nextBillingAt }

      // plusMonths 만 쓰면 2/28 → 3/28 → 4/28 로 당겨진 채 남는다
      assertEquals(
        listOf(
          LocalDateTime.of(2026, 2, 28, 10, 0),
          LocalDateTime.of(2026, 3, 31, 10, 0),
          LocalDateTime.of(2026, 4, 30, 10, 0),
        ),
        dates,
      )
    }
  }

  @Nested
  inner class 결제_결과_반영 {

    @Test
    fun `결제 성공 시 예약해 둔 계약 변경을 적용하고 송장 잠금을 푼다`() {
      val sub = subscription(nextBillingAt = LocalDateTime.of(2026, 2, 1, 10, 0))
      sub.lockGeneratingInvoice()
      val newPolicy = monthlyPolicy(retryCount = 5)
      sub.changeBillingPolicy(newPolicy)

      sub.paySuccess(payVersion = 1)

      assertEquals(newPolicy, sub.planSnapshot.billingPolicy)
      assertNull(sub.nextSnapshot)
      assertFalse(sub.isLockGeneratingInvoice)
      assertEquals(1L, sub.payVersion)
      assertEquals(SubscriptionStatus.ACTIVE, sub.status)
      assertEquals(LocalDateTime.of(2026, 3, 1, 10, 0), sub.nextBillingAt)
    }

    @Test
    fun `결제 실패는 재시도 횟수를 넘기 전까지 결제 실패 상태로 두고, 넘으면 비활성화한다`() {
      val sub = subscription(snapshot = snapshot(policy = monthlyPolicy(retryCount = 3)))

      sub.payFail(retryCount = 3)
      assertEquals(SubscriptionStatus.PAYMENT_ON_FAILURE, sub.status)
      assertTrue(sub.isLockGeneratingInvoice, "재시도 중에는 새 송장을 만들지 않는다")

      sub.payFail(retryCount = 4)
      assertEquals(SubscriptionStatus.INACTIVE, sub.status)
    }
  }
}
