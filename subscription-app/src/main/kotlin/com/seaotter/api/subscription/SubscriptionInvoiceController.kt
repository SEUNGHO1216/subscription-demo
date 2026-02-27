package com.seaotter.api.subscription

import com.seaotter.core.application.subscription.service.SubscriptionInvoiceQueryService
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.SubscriptionStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/invoice")
class SubscriptionInvoiceController(
  private val subscriptionInvoiceQueryService: SubscriptionInvoiceQueryService
) {

  @GetMapping("/{id}")
  fun findById(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String
  ): SubscriptionInvoice? {
    return subscriptionInvoiceQueryService.findById(enterpriseId, id)
  }

  @GetMapping("/subscription/{subscriptionId}")
  fun findAllBySubscriptionIdAndPayVersionNullable(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("subscriptionId") subscriptionId: String,
    @RequestParam(required = false, value = "payVersion") payVersion: Long?
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryService.findAllBySubscriptionIdAndPayVersionNullable(
      enterpriseId, subscriptionId, payVersion
    )
  }

  @GetMapping("/billing-date/status")
  fun findAllBySubscriptionIdAndPayVersionNullable(
    @RequestParam(required = false, value = "enterpriseId") enterpriseId: String?,
    @RequestParam(required = false, value = "id") id: String?,
    @RequestParam(value = "date") date: LocalDate,
    @RequestParam(value = "status") status: InvoiceStatus,
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryService.findAllByTargetBillingDateAndStatus(
      enterpriseId, id, date, status
    )
  }
}