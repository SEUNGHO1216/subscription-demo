package com.seaotter.api.subscription

import com.seaotter.core.application.subscription.dto.SubscriptionPlanCommandRequest
import com.seaotter.core.application.subscription.dto.SubscriptionPlanDTO
import com.seaotter.core.application.subscription.service.SubscriptionPlanCommandService
import com.seaotter.core.application.subscription.service.SubscriptionPlanQueryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/plan")
class SubscriptionPlanController(
  private val subscriptionPlanCommandService: SubscriptionPlanCommandService,
  private val subscriptionPlanQueryService: SubscriptionPlanQueryService,
) {

  @PostMapping
  fun createPlan(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestBody createRequest: SubscriptionPlanCommandRequest
  ): SubscriptionPlanDTO {
    return subscriptionPlanCommandService.createPlan(enterpriseId, createRequest)
  }

  @PutMapping("/{id}")
  fun updatePlan(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String,
    @RequestBody updateRequest: SubscriptionPlanCommandRequest
  ): SubscriptionPlanDTO {
    return subscriptionPlanCommandService.updatePlan(enterpriseId, id, updateRequest)
  }

  @PutMapping("/active")
  fun activate(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ): List<SubscriptionPlanDTO> {
    return subscriptionPlanCommandService.activate(enterpriseId, idList)
  }

  @PutMapping("/inactive")
  fun inactivate(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ): List<SubscriptionPlanDTO> {
    return subscriptionPlanCommandService.inactivate(enterpriseId, idList)
  }

  @DeleteMapping()
  fun deletePlans(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ) {
    subscriptionPlanCommandService.deletePlan(enterpriseId, idList)
  }


  @GetMapping("/{id}")
  fun findById(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String,
  ): SubscriptionPlanDTO? {
    return subscriptionPlanQueryService.findById(enterpriseId, id)
  }

  @GetMapping("/list")
  fun findAllByIdList(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryService.findAllByIdList(enterpriseId, idList)
  }

  @GetMapping("/store/product")
  fun findAllByStoreIdAndSubscriptionProductId(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam(required = false, value = "storeId") storeId: String?,
    @RequestParam(required = false, value = "id") id: String?,
  ): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryService.findAllByStoreIdAndSubscriptionProductId(enterpriseId, storeId, id)
  }

  @GetMapping("/product-list")
  fun findAllByProductIdList(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam(value = "productIdList") productIdList: List<String>,
  ): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryService.findAllByProductIdList(enterpriseId, productIdList)
  }

}