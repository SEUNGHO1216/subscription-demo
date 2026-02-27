package com.seaotter.api.billing

import com.seaotter.core.application.billing.service.PaymentCommandService
import com.seaotter.core.application.billing.service.PaymentQueryService
import com.seaotter.core.domain.billing.Payment
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse
import com.seaotter.core.domain.billing.dto.PayCancelRequestByInvoice
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment")
class PaymentController(
  private val paymentCommandService: PaymentCommandService,
  private val paymentQueryService: PaymentQueryService
) {

  @PostMapping
  fun pay(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestBody billingKeyPayRequest: BillingKeyPayRequest
  ): BillingKeyPayResponse {
    return paymentCommandService.pay(enterpriseId, billingKeyPayRequest)
  }

  @PostMapping("/cancel")
  fun payCancel(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestBody billingKeyPayCancelRequest: BillingKeyPayCancelRequest
  ): BillingKeyPayCancelResponse {
    return paymentCommandService.payCancel(enterpriseId, billingKeyPayCancelRequest)
  }

  @PostMapping("/cancel/by-invoice")
  fun payCancelByInvoice(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestBody paymentCancelRequestByInvoice: PayCancelRequestByInvoice
  ): BillingKeyPayCancelResponse {
    return paymentCommandService.payCancelByInvoiceInfo(
      enterpriseId,
      paymentCancelRequestByInvoice.invoiceId,
      paymentCancelRequestByInvoice.amount
    )
  }

  @GetMapping("/{id}")
  fun findById(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String
  ): Payment? {
    return paymentQueryService.findById(enterpriseId, id)
  }

  @GetMapping("/member/{memberId}")
  fun findAllByMemberId(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("memberId") memberId: String
  ): List<Payment> {
    return paymentQueryService.findAllByMemberId(enterpriseId, memberId)
  }

  @GetMapping("/invoice/{invoiceId}")
  fun findByInvoiceIdAndPayVersion(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("invoiceId") invoiceId: String
  ): Payment? {
    return paymentQueryService.findSuccessPaymentByInvoiceIdAndPayVersion(enterpriseId, invoiceId)
  }

  @GetMapping("/subscription/{subscriptionId}")
  fun findAllBySubscriptionIdAndPayVersion(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("subscriptionId") subscriptionId: String,
  ): List<Payment> {
    return paymentQueryService.findAllBySubscriptionId(enterpriseId, subscriptionId)
  }
}