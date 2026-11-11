package com.seaotter.core.domain.subscription

import com.seaotter.core.domain.subscription.SubscriptionFixture.invoice
import com.seaotter.core.domain.subscription.SubscriptionFixture.monthlyPolicy
import com.seaotter.core.domain.subscription.SubscriptionFixture.snapshot
import com.seaotter.core.domain.subscription.SubscriptionFixture.subscription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SubscriptionInvoiceTest {

  private val sub = subscription(snapshot = snapshot(policy = monthlyPolicy(retryCount = 3)))

  @Test
  fun `결제 실패는 재시도 횟수까지 실패 상태로 남고, 넘으면 폐기된다`() {
    val invoice = invoice(sub)

    repeat(3) { invoice.failPay() }
    assertEquals(InvoiceStatus.PAYMENT_ON_FAILURE, invoice.state.status)
    assertEquals(3, invoice.state.retryCount)

    invoice.failPay()
    assertEquals(InvoiceStatus.DISCARDED, invoice.state.status)
  }

  @Test
  fun `초안과 결제 실패 송장만 결제를 위해 선점할 수 있다`() {
    val draft = invoice(sub)
    draft.startProcessing()
    assertEquals(InvoiceStatus.PROCESSING, draft.state.status)

    val failed = invoice(sub).apply { failPay() }
    failed.startProcessing()
    assertEquals(InvoiceStatus.PROCESSING, failed.state.status)
  }

  @Test
  fun `이미 처리 중이거나 끝난 송장은 다시 선점할 수 없다`() {
    val processing = invoice(sub).apply { startProcessing() }
    assertThrows<IllegalStateException> { processing.startProcessing() }

    val resolved = invoice(sub).apply { successPay() }
    assertThrows<IllegalStateException> { resolved.startProcessing() }
  }

  @Test
  fun `잠긴 구독으로는 송장을 만들 수 없다`() {
    assertThrows<IllegalArgumentException> {
      SubscriptionInvoice(
        enterpriseId = sub.enterpriseId,
        subscriptionId = sub.id,
        memberId = sub.memberId,
        storeId = sub.storeId,
        planSnapshot = sub.planSnapshot,
        payVersion = 0,
        targetBillingAt = sub.createdAt,
        billKey = sub.billingKey,
        lockGeneratingInvoice = true,
      )
    }
  }
}
