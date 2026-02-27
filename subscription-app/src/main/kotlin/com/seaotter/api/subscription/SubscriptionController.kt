package com.seaotter.api.subscription

import com.seaotter.core.application.subscription.dto.ManuallySchedulerCommandRequest
import com.seaotter.core.application.subscription.dto.SubscriptionDTO
import com.seaotter.core.application.subscription.service.SubscriptionCommandService
import com.seaotter.core.application.subscription.service.SubscriptionQueryService
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionBillingPolicy
import com.seaotter.core.domain.subscription.SubscriptionStatus
import com.seaotter.core.domain.subscription.dto.SubscriptionStartRequestDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/subscription")
class SubscriptionController(
  private val subscriptionCommandService: SubscriptionCommandService,
  private val subscriptionQueryService: SubscriptionQueryService
) {

  @PostMapping()
  fun startSubscription(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestBody startRequest: SubscriptionStartRequestDTO
  ): SubscriptionDTO {
    return subscriptionCommandService.startSubscription(enterpriseId, startRequest)
  }

  @PostMapping("/{id}/termination")
  fun terminateSubscription(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String
  ): SubscriptionDTO {
    return subscriptionCommandService.terminateSubscription(enterpriseId, id)
  }

  @PutMapping("/{id}/termination-cancel")
  fun cancelTerminateSubscription(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String
  ): SubscriptionDTO {
    return subscriptionCommandService.cancelTerminateSubscription(enterpriseId, id)
  }

  @PutMapping("/{id}/pay-date")
  fun updatePayDateInfo(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String,
    @RequestBody subscriptionBillingPolicy: SubscriptionBillingPolicy
  ): SubscriptionDTO {
    return subscriptionCommandService.updatePayDate(enterpriseId, id, subscriptionBillingPolicy)
  }

  @PutMapping("/{id}/activation")
  fun toggleActivation(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String,
  ): SubscriptionDTO {
    return subscriptionCommandService.toggleActivation(enterpriseId, id)
  }

  @PostMapping("/manual/scheduler/invoice")
  fun generateInvoiceManually(
    @RequestBody request: ManuallySchedulerCommandRequest
  ) {
    subscriptionCommandService.generateInvoice(request.enterpriseId, request.id, request.date)
  }

  @PostMapping("/manaul/scheduler/terminate-complete")
  fun terminateCompleteManually(
    @RequestBody request: ManuallySchedulerCommandRequest
  ) {
    subscriptionCommandService.terminateCompleteSubscription(request.enterpriseId, request.id, request.date)
  }

  @PostMapping("/manaul/scheduler/integrate-billing")
  fun integratedBillingInvoiceManually(
    @RequestBody request: ManuallySchedulerCommandRequest
  ) {
    subscriptionCommandService.integratedBillingSubscription(request.enterpriseId, request.date)
  }

  @PostMapping("/manaul/scheduler/target-billing")
  fun targetBillingInvoiceManually(
    @RequestBody request: ManuallySchedulerCommandRequest,
    @RequestParam(value = "invoiceStatus") invoiceStatus: InvoiceStatus
  ) {
    subscriptionCommandService.targetBillingSubscription(
      request.enterpriseId,
      request.id,
      request.date,
      invoiceStatus
    )
  }

  @GetMapping("/{id}")
  fun findById(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String
  ): Subscription? {
    return subscriptionQueryService.findById(enterpriseId, id)
  }

  @GetMapping("/member/{memberId}")
  fun findAllByMemberId(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("memberId") memberId: String
  ): List<Subscription> {
    return subscriptionQueryService.findAllByMemberId(enterpriseId, memberId)
  }

  @GetMapping("/billing-date/status")
  fun findAllByNextBillingDateAndStatus(
    @RequestParam(required = false, value = "enterpriseId") enterpriseId: String?,
    @RequestParam(required = false, value = "id") id: String?,
    @RequestParam(value = "date") date: LocalDate,
    @RequestParam(value = "status") status: SubscriptionStatus,
  ): List<Subscription> {
    return subscriptionQueryService.findAllByNextBillingDateAndStatus(enterpriseId, id, date, status)
  }
}