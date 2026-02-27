package com.seaotter.infra.billing.persistence.entity

import com.seaotter.core.domain.billing.Refund
import com.seaotter.infra.billing.persistence.entity.id.RefundEntityId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Table(
  name = "refund",
  indexes = [
    Index(name = "idx_enterprise_payment", columnList = "enterprise_id, payment_id"),
  ]
)
@Entity
class RefundEntity(
  @EmbeddedId
  val id: RefundEntityId,
  val paymentId: String,
  val memberId: String,
  val storeId: String,
  val resultCode: String,
  val resultMsg: String,
  val tid: String,
  val cancelDate: String,
  val cancelTime: String,
  val cancelAmount: Long,
  val remainAmount: Long,
  val createdAt: LocalDateTime = LocalDateTime.now(),
) {

  fun toDomain(): Refund {
    return Refund(
      enterpriseId = this.id.enterpriseId,
      id= this.id.refundId,
      paymentId = this.paymentId,
      memberId = this.memberId,
      storeId = this.storeId,
      resultCode = this.resultCode,
      resultMsg = this.resultMsg,
      tid = this.tid,
      cancelDate = this.cancelDate,
      cancelTime = this.cancelTime,
      cancelAmount = this.cancelAmount,
      remainAmount = this.remainAmount,
      createdAt = this.createdAt,
    )
  }

  companion object {
    fun toEntity(domain: Refund): RefundEntity {
      return RefundEntity(
        id = RefundEntityId(
          domain.enterpriseId,
          domain.id
        ),
        paymentId = domain.paymentId,
        memberId = domain.memberId,
        storeId = domain.storeId,
        resultCode = domain.resultCode,
        resultMsg = domain.resultMsg,
        tid = domain.tid,
        cancelDate = domain.cancelDate,
        cancelTime = domain.cancelTime,
        cancelAmount = domain.cancelAmount,
        remainAmount = domain.remainAmount,
        createdAt = domain.createdAt,
      )
    }
  }
}