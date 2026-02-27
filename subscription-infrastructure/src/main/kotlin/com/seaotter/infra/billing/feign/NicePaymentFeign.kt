package com.seaotter.infra.billing.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(
  name = "NicePaymentFeign",
  url = "https://webapi.nicepay.co.kr/webapi",
  dismiss404 = true,
  configuration = [NicePaymentFeignConfigKotlin::class]
)
interface NicePaymentFeign {

  @PostMapping(
    value = ["/billing/billing_regist.jsp"],
    produces = ["application/json"],
    consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
  )
  fun registerBillKey(
    @RequestBody billingRegisterRequest: NicePaymentBillingRegisterRequest
  ): NicePaymentBillingRegisterResponse

  @PostMapping(
    value = ["/billing/billing_approve.jsp"],
    produces = ["application/json"],
    consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
  )
  fun payBillKey(
    @RequestBody billingPayRequest: NicePaymentPayRequest
  ): NicePaymentPayResponse

  @PostMapping(
    value = ["/cancel_process.jsp"],
    produces = ["application/json"],
    consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
  )
  fun cancelBilling(
    @RequestBody cancelProcessRequest: NicePaymentCancelProcessRequest
  ): NicePaymentCancelProcessResponse
}