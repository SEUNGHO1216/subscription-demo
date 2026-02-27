package com.seaotter.api.subscription

import com.seaotter.core.application.subscription.dto.SubscriptionProductCommandRequest
import com.seaotter.core.application.subscription.dto.SubscriptionProductDTO
import com.seaotter.core.application.subscription.dto.SubscriptionProductWithPlansResponse
import com.seaotter.core.application.subscription.service.SubscriptionProductCommandService
import com.seaotter.core.application.subscription.service.SubscriptionProductQueryService
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
@RequestMapping("/product")
class SubscriptionProductController(
  private val subscriptionProductCommandService: SubscriptionProductCommandService,
  private val subscriptionProductQueryService: SubscriptionProductQueryService
){

  @PostMapping
  fun createProduct(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestBody createRequest: SubscriptionProductCommandRequest
  ): SubscriptionProductDTO {
    return subscriptionProductCommandService.createProduct(enterpriseId, createRequest)
  }

  @PutMapping("/{id}")
  fun updateProduct(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String,
    @RequestBody updateRequest: SubscriptionProductCommandRequest
  ): SubscriptionProductDTO {
    return subscriptionProductCommandService.updateProduct(enterpriseId, id, updateRequest)
  }

  @PutMapping("/active")
  fun activate(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ): List<SubscriptionProductDTO> {
    return subscriptionProductCommandService.activate(enterpriseId, idList)
  }

  @PutMapping("/inactive")
  fun inactivate(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ): List<SubscriptionProductDTO> {
    return subscriptionProductCommandService.inactivate(enterpriseId, idList)
  }

  @DeleteMapping("/{idList}")
  fun deleteProductList(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ) {
    subscriptionProductCommandService.deleteProduct(enterpriseId, idList)
  }

  @GetMapping("/{id}")
  fun findById(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String,
  ): SubscriptionProductDTO? {
    return subscriptionProductQueryService.findById(enterpriseId, id)
  }

  @GetMapping("/{id}/plans")
  fun findByIdWithPlans(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("id") id: String,
  ): SubscriptionProductWithPlansResponse? {
    return subscriptionProductQueryService.findByIdWithPlans(enterpriseId, id)
  }

  @GetMapping("/store/{storeId}")
  fun findAllByStoreId(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("storeId") storeId: String,
  ): List<SubscriptionProductDTO> {
    return subscriptionProductQueryService.findAllByStoreId(enterpriseId, storeId)
  }

  @GetMapping("/store/{storeId}/plans")
  fun findAllByStoreIdWithPlans(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @PathVariable("storeId") storeId: String,
  ): List<SubscriptionProductWithPlansResponse> {
    return subscriptionProductQueryService.findAllByStoreIdWithPlans(enterpriseId, storeId)
  }

  @GetMapping("/list")
  fun findAllByIdList(
    @RequestHeader("enterpriseId") enterpriseId: String,
    @RequestParam("idList") idList: List<String>,
  ): List<SubscriptionProductDTO> {
    return subscriptionProductQueryService.findAllByIdList(enterpriseId, idList)
  }
}