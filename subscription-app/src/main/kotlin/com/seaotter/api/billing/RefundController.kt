package com.seaotter.api.billing

import com.seaotter.core.application.billing.service.RefundQueryService
import com.seaotter.core.domain.billing.Refund
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Ref

@RestController
@RequestMapping("/refund")
class RefundController(
  private val refundQueryService: RefundQueryService
) {

  @GetMapping("/payment/{paymentId}")
  fun findAllByPaymentId(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("paymentId") paymentId: String
  ): List<Refund> {
    return refundQueryService.findAllByPaymentId(enterpriseId, paymentId)
  }

  @GetMapping("/member/{memberId}")
  fun findAllByMemberId(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("memberId") memberId: String
  ): List<Refund> {
    return refundQueryService.findAllByMemberId(enterpriseId, memberId)
  }
}