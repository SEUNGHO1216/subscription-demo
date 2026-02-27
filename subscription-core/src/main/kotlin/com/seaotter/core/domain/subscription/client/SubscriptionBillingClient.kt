package com.seaotter.core.domain.subscription.client

import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse

interface SubscriptionBillingClient {
  fun pay(enterpriseId: String, billingKeyPayRequest: BillingKeyPayRequest): BillingKeyPayResponse
}