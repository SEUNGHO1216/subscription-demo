package com.seaotter.infra.billing.persistence.entity

import com.seaotter.core.domain.billing.Payment
import com.seaotter.core.domain.billing.PaymentResponseInfo
import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.infra.billing.persistence.entity.id.PaymentEntityId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Table(
  name = "payment",
  indexes = [
    Index(name = "idx_enterprise_member", columnList = "enterprise_id, member_id"),
    Index(name = "idx_enterprise_invoice", columnList = "enterprise_id, subscription_invoice_id"),
    Index(name = "idx_enterprise_subscription", columnList = "enterprise_id, subscription_id")
  ]
)
@Entity
class PaymentEntity(
  @EmbeddedId
  val id: PaymentEntityId,
  val orderId: String,
  val memberId: String,
  val storeId: String,
  val subscriptionId: String,
  val payVersion: Long,
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(value = SqlTypes.VARCHAR)
  val van: VanCompany = VanCompany.NICE_PAYMENTS,
  val tid: String,
  val resultCode: String,
  val resultMsg: String,
  val approvalId: String,
  val approvalDate: String,
  val amount: Long,
  val installment: Int = 0,
  val cardCode: String,
  val cardName: String,
  val cardNumber: String,
  val isCheckCard: Boolean,
  val canPartiallyCancel: Boolean,
  val subscriptionInvoiceId: String,
  val goodsName: String,
  val billingKey: String,
  val createdAt: LocalDateTime,
) {
  var canceledAt: LocalDateTime? = null

  fun toDomain(): Payment {
    val payment = Payment(
      enterpriseId = this.id.enterpriseId,
      id = this.id.paymentId,
      orderId = this.orderId,
      memberId = this.memberId,
      storeId = this.storeId,
      subscriptionId = this.subscriptionId,
      payVersion = this.payVersion,
      van = this.van,
      paymentInfo = PaymentResponseInfo(
        tid = this.tid,
        resultCode = this.resultCode,
        resultMsg = this.resultMsg,
        approvalId = this.approvalId,
        approvalDate = this.approvalDate,
        amount = this.amount,
        installment = this.installment,
        cardCode = this.cardCode,
        cardName = this.cardName,
        cardNumber = this.cardNumber,
        isCheckCard = this.isCheckCard,
        canPartiallyCancel = this.canPartiallyCancel,
      ),
      createdAt = createdAt,
      subscriptionInvoiceId = subscriptionInvoiceId,
      goodsName = goodsName,
      billingKey = this.billingKey,
    )
    payment.canceledAt = this.canceledAt
    return payment
  }

  companion object {
    fun toEntity(payment: Payment): PaymentEntity {
      val entity = PaymentEntity(
        id = PaymentEntityId(
          payment.enterpriseId,
          payment.id
        ),
        orderId = payment.orderId,
        memberId = payment.memberId,
        storeId = payment.storeId,
        subscriptionId = payment.subscriptionId,
        payVersion = payment.payVersion,
        van = payment.van,
        tid = payment.paymentInfo.tid,
        resultCode = payment.paymentInfo.resultCode,
        resultMsg = payment.paymentInfo.resultMsg,
        approvalId = payment.paymentInfo.approvalId,
        approvalDate = payment.paymentInfo.approvalDate,
        amount = payment.paymentInfo.amount,
        installment = payment.paymentInfo.installment,
        cardCode = payment.paymentInfo.cardCode,
        cardName = payment.paymentInfo.cardName,
        cardNumber = payment.paymentInfo.cardNumber,
        isCheckCard = payment.paymentInfo.isCheckCard,
        canPartiallyCancel = payment.paymentInfo.canPartiallyCancel,
        subscriptionInvoiceId = payment.subscriptionInvoiceId,
        goodsName = payment.goodsName,
        billingKey = payment.billingKey,
        createdAt = payment.createdAt,
      )
      entity.canceledAt = payment.canceledAt
      return entity
    }
  }
}

