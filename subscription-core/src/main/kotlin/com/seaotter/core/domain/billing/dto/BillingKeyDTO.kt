package com.seaotter.core.domain.billing.dto

import com.seaotter.core.domain.billing.PaymentResponseInfo
import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.core.domain.subscription.SubscriptionInvoice


data class BillingKeyPayRequest(
  val van: VanCompany? = null,
  val memberId: String,
  val storeId: String,
  val subscriptionId: String,
  val subscriptionInvoiceId: String,
  val goodsName: String,
  val amount: Long,
  val billKey: String,
  val installment: Int = 0,
  val orderId: String? = null,
  val payVersion: Long,
) {
  constructor(
    van: VanCompany? = null,
    installment: Int,
    subscriptionInvoice: SubscriptionInvoice,
  ): this(
    van = van,
    memberId = subscriptionInvoice.memberId,
    storeId = subscriptionInvoice.storeId,
    subscriptionId = subscriptionInvoice.subscriptionId,
    subscriptionInvoiceId = subscriptionInvoice.id,
    goodsName = subscriptionInvoice.planSnapshot.name,
    amount = subscriptionInvoice.state.price,
    billKey = subscriptionInvoice.billKey,
    installment = installment,
    payVersion = subscriptionInvoice.payVersion,
  )
}

data class BillingKeyPayResponse(
  val billingKey: String,
  val orderId: String,
  val amount: Long,
  val resultCode: String,
  val resultMsg: String,
  val tid: String,
  val authCode: String,
  val authDate: String,
  val cardCode: String, //issuer 만
  val cardName: String,
  val cardNumber: String,
  val installment: Int,
  val isCheckCard: Boolean,
  val canPartiallyCancel: Boolean,
) {
  fun toPaymentResponseInfo(): PaymentResponseInfo {
    return PaymentResponseInfo(
      tid = tid,
      resultCode = resultCode,
      resultMsg = resultMsg,
      approvalId = authCode,
      approvalDate = authDate,
      amount = amount,
      installment = installment,
      cardCode = cardCode,
      cardName = cardName,
      cardNumber = cardNumber,
      isCheckCard = isCheckCard,
      canPartiallyCancel = canPartiallyCancel,
    )
  }
}

data class BillingKeyPayCancelRequest(
  val van: VanCompany,
  val paymentId: String,
  val orderId: String,
  val tid: String?,
  val memberId: String,
  val subscriptionInvoiceId: String?,
  val amount: Long,
  val isPartiallyCancel: Boolean,
  val cancelMessage: String = "빌링 결제 취소",
)

data class BillingKeyPayCancelResponse(
  val orderId: String,
  val canceledAmount: Long,
  val resultCode: String,
  val resultMsg: String,
  val tid: String,
  val cancelDate: String,
  val cancelTime: String,
  val remainAmount: Long,
)

data class PayCancelRequestByInvoice(
  val invoiceId: String,
  val amount: Long
)