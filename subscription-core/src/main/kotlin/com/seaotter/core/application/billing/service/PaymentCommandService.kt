package com.seaotter.core.application.billing.service

import com.seaotter.core.application.billing.port.BillingKeyPgPort
import com.seaotter.core.application.billing.port.PaymentCommandPersistencePort
import com.seaotter.core.application.billing.port.PaymentQueryPersistencePort
import com.seaotter.core.application.billing.port.RefundCommandPersistencePort
import com.seaotter.core.application.billing.port.RefundQueryPersistencePort
import com.seaotter.core.domain.billing.Payment
import com.seaotter.core.domain.billing.Refund
import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
@Service
class PaymentCommandService(
  private val paymentCommandPersistencePort: PaymentCommandPersistencePort,
  private val paymentQueryPersistencePort: PaymentQueryPersistencePort,
  private val refundCommandPersistencePort: RefundCommandPersistencePort,
  private val refundQueryPersistencePort: RefundQueryPersistencePort,
  private val billingKeyPgPort: BillingKeyPgPort
) {

  fun pay(enterpriseId: String, billingKeyPayRequest: BillingKeyPayRequest): BillingKeyPayResponse {

    val orderId = billingKeyPayRequest.orderId ?: UUID.randomUUID().toString()
    val van = billingKeyPayRequest.van?: getVanCompanyByEnterpriseId(enterpriseId)
    // subscription 생성이 전제
    // 빌키 결제 진행
    val result = billingKeyPgPort.pay(billingKeyPayRequest.copy(orderId = orderId, van = van))
    // 결제 저장
    Payment(
      enterpriseId = enterpriseId,
      orderId = orderId,
      memberId = billingKeyPayRequest.memberId,
      storeId = billingKeyPayRequest.storeId,
      subscriptionId = billingKeyPayRequest.subscriptionId,
      payVersion = billingKeyPayRequest.payVersion,
      van = billingKeyPayRequest.van?: getVanCompanyByEnterpriseId(enterpriseId),
      subscriptionInvoiceId = billingKeyPayRequest.subscriptionInvoiceId,
      goodsName = billingKeyPayRequest.goodsName,
      billingKey = billingKeyPayRequest.billKey,
      paymentInfo = result.toPaymentResponseInfo(),
    ).run { paymentCommandPersistencePort.save(this) }
    // (subscription 후처리) : 이건 나중에?
    return result;
  }

  fun payCancel(enterpriseId: String, billingKeyPayCancelRequest: BillingKeyPayCancelRequest): BillingKeyPayCancelResponse {
    val targetPayment = requireNotNull(paymentQueryPersistencePort.findById(enterpriseId, billingKeyPayCancelRequest.paymentId)) {
      "취소할 결제 건이 없습니다."
    }
    val result = billingKeyPgPort.payCancel(
      billingKeyPayCancelRequest.copy(
        tid = targetPayment.paymentInfo.tid,
        isPartiallyCancel = targetPayment.paymentInfo.amount > billingKeyPayCancelRequest.amount
      )
    )
    // 환불로 인한 각종 상태변환 > 찐 환불처리
    when(result.resultCode) {
      "2001" -> _cancelSuccess(enterpriseId, targetPayment, result)
      else -> _cancelFail(enterpriseId, targetPayment, result)
    }

    return result;
  }

  fun payCancelByInvoiceInfo(
    enterpriseId: String,
    invoiceId: String,
    cancelAmount: Long
  ): BillingKeyPayCancelResponse {
    val targetPayment =
      requireNotNull(paymentQueryPersistencePort.findSuccessPaymentByInvoiceId(enterpriseId, invoiceId)) {
        "취소할 결제 건이 없습니다."
      }
    val result = billingKeyPgPort.payCancel(
      BillingKeyPayCancelRequest(
        van = targetPayment.van,
        paymentId = targetPayment.id,
        orderId = targetPayment.orderId,
        tid = targetPayment.paymentInfo.tid,
        memberId = targetPayment.memberId,
        subscriptionInvoiceId = targetPayment.subscriptionInvoiceId,
        amount = cancelAmount,
        isPartiallyCancel = targetPayment.paymentInfo.amount > cancelAmount
      )
    )
    // 환불로 인한 각종 상태변환 > 찐 환불처리
    when(result.resultCode) {
      "2001" -> _cancelSuccess(enterpriseId, targetPayment, result)
      else -> _cancelFail(enterpriseId, targetPayment, result)
    }

    return result;
  }

  private fun _cancelSuccess(enterpriseId: String, targetPayment: Payment, result: BillingKeyPayCancelResponse) {
    targetPayment.cancel().also {
      paymentCommandPersistencePort.save(it)
      refundCommandPersistencePort.save(
        Refund(
          enterpriseId = enterpriseId,
          paymentId = targetPayment.id,
          memberId = targetPayment.memberId,
          storeId = targetPayment.storeId,
          resultCode = result.resultCode,
          resultMsg = result.resultMsg,
          tid = result.tid,
          cancelDate = result.cancelDate,
          cancelTime = result.cancelTime,
          cancelAmount = result.canceledAmount,
          remainAmount = result.remainAmount,
        )
      )
    }
  }

  private fun _cancelFail(enterpriseId: String, targetPayment: Payment, result: BillingKeyPayCancelResponse) {
    refundCommandPersistencePort.save(
      Refund(
        enterpriseId = enterpriseId,
        paymentId = targetPayment.id,
        memberId = targetPayment.memberId,
        storeId = targetPayment.storeId,
        resultCode = result.resultCode,
        resultMsg = result.resultMsg,
        tid = result.tid,
        cancelDate = result.cancelDate,
        cancelTime = result.cancelTime,
        cancelAmount = result.canceledAmount,
        remainAmount = result.remainAmount,
      )
    )
  }

  // 일단 가라로
  private fun getVanCompanyByEnterpriseId(enterpriseId: String): VanCompany {
    return VanCompany.NICE_PAYMENTS
  }
}