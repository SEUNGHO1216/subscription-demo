package com.seaotter.core.application.subscription.port.`in`

import com.seaotter.core.application.billing.service.PaymentCommandService
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse
import com.seaotter.core.domain.subscription.client.SubscriptionBillingClient
import org.springframework.stereotype.Component

@Component
class SubscriptionBillingClientImpl(
  private val paymentCommandService: PaymentCommandService
): SubscriptionBillingClient {

  override fun pay(enterpriseId:String, billingKeyPayRequest: BillingKeyPayRequest): BillingKeyPayResponse {
    return paymentCommandService.pay(enterpriseId, billingKeyPayRequest)
  }
}