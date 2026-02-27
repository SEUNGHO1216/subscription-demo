package com.seaotter.core.application.billing.port

import com.seaotter.core.application.billing.dto.BillingKeyRegisterRequest
import com.seaotter.core.application.billing.dto.BillingKeyRegisterResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse

interface BillingKeyPgPort {
  fun register(billingKeyRegisterRequest: BillingKeyRegisterRequest): BillingKeyRegisterResponse
  fun pay(billingKeyPayRequest: BillingKeyPayRequest): BillingKeyPayResponse
  fun payCancel(billingKeyPayCancelRequest: BillingKeyPayCancelRequest): BillingKeyPayCancelResponse
}