package com.seaotter.infra.billing.adapter

import com.seaotter.core.application.billing.dto.BillingKeyRegisterRequest
import com.seaotter.core.application.billing.dto.BillingKeyRegisterResponse
import com.seaotter.core.application.billing.port.BillingKeyPgPort
import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse
import com.seaotter.infra.billing.pg.NicePaymentBillingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BillingKeyPgAdapter(
  private val nicePaymentBillingService: NicePaymentBillingService,
): BillingKeyPgPort {

  val log = LoggerFactory.getLogger(this::class.java)

  override fun register(billingKeyRegisterRequest: BillingKeyRegisterRequest): BillingKeyRegisterResponse {
    return when(billingKeyRegisterRequest.van) {
      VanCompany.NICE_PAYMENTS -> {nicePaymentBillingService.registerBillingKey(billingKeyRegisterRequest) }
      else -> throw RuntimeException("Not supported billing key van")
    }
  }

  override fun pay(billingKeyPayRequest: BillingKeyPayRequest): BillingKeyPayResponse {
    return when(billingKeyPayRequest.van) {
      VanCompany.NICE_PAYMENTS -> {nicePaymentBillingService.pay(billingKeyPayRequest) }
      else -> throw RuntimeException("Not supported billing key van")
    }
  }

  override fun payCancel(billingKeyPayCancelRequest: BillingKeyPayCancelRequest): BillingKeyPayCancelResponse {
    return when(billingKeyPayCancelRequest.van) {
      VanCompany.NICE_PAYMENTS -> {nicePaymentBillingService.payCancel(billingKeyPayCancelRequest) }
      else -> throw RuntimeException("Not supported billing key van")
    }
  }
}