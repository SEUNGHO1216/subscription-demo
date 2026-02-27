package com.seaotter.api.billing

import com.seaotter.core.application.billing.dto.BillingKeyDTO
import com.seaotter.core.application.billing.dto.BillingKeyRegisterRequest
import com.seaotter.core.application.billing.dto.BillingKeyRegisterResponse
import com.seaotter.core.application.billing.service.BillingKeyCommandService
import com.seaotter.core.application.billing.service.BillingKeyQueryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/billing-key")
class BillingKeyController(
  private val billingKeyCommandService: BillingKeyCommandService,
  private val billingKeyQueryService: BillingKeyQueryService
) {

  @PostMapping
  fun registerBillingKey(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestBody billingKeyRegisterRequest: BillingKeyRegisterRequest
  ): BillingKeyRegisterResponse {
    return billingKeyCommandService.register(enterpriseId, billingKeyRegisterRequest)
  }

  @DeleteMapping()
  fun registerBillingKey(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>
  ) {
    billingKeyCommandService.delete(enterpriseId, idList)
  }

  @GetMapping("/{id}")
  fun findById(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String
  ): BillingKeyDTO? {
    return billingKeyQueryService.findById(enterpriseId, id)
  }

  @GetMapping("/member/{memberId}")
  fun findAllByMemberId(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("memberId") memberId: String
  ): List<BillingKeyDTO> {
    return billingKeyQueryService.findAllByMemberId(enterpriseId, memberId)
  }
}