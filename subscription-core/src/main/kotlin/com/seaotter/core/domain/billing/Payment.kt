package com.seaotter.core.domain.billing

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Payment(
  val enterpriseId: String,
  var orderId: String,
  val memberId: String,
  val storeId: String,
  val subscriptionId: String,
  val payVersion: Long,
  val van: VanCompany = VanCompany.NICE_PAYMENTS,
  val subscriptionInvoiceId: String,
  val goodsName: String,
  val billingKey: String,
  val paymentInfo: PaymentResponseInfo,
  val createdAt: LocalDateTime = LocalDateTime.now(),
  val id: String = "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}-$memberId-$subscriptionInvoiceId",
) {
  var canceledAt: LocalDateTime? = null

  fun cancel(): Payment {
    this.canceledAt = LocalDateTime.now()
    return this
  }
}

data class PaymentResponseInfo(
  val tid: String,
  val resultCode: String,
  val resultMsg: String,
  val approvalId: String,
  val approvalDate: String,
  val amount: Long,
  val installment: Int=0,
  val cardCode: String,
  val cardName: String,
  val cardNumber: String,
  val isCheckCard: Boolean,
  val canPartiallyCancel: Boolean,
  )