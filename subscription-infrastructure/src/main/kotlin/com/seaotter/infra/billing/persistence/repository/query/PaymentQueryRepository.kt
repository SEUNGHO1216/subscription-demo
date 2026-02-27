package com.seaotter.infra.billing.persistence.repository.query

import com.querydsl.jpa.impl.JPAQueryFactory
import com.seaotter.infra.billing.persistence.entity.PaymentEntity
import com.seaotter.infra.billing.persistence.entity.QPaymentEntity
import com.seaotter.infra.billing.persistence.entity.id.PaymentEntityId
import org.springframework.stereotype.Repository

@Repository
class PaymentQueryRepository(
  private val jpaQueryFactory: JPAQueryFactory,
  private val payment: QPaymentEntity = QPaymentEntity.paymentEntity
) {

  fun findById(
    enterpriseId: String,
    paymentId: String
  ): PaymentEntity? {
    return jpaQueryFactory.selectFrom(payment)
      .where(payment.id.eq(PaymentEntityId(enterpriseId, paymentId)))
      .fetchFirst()
  }

  fun findSuccessPaymentByInvoiceId(
    enterpriseId: String,
    invoiceId: String,
  ): PaymentEntity? {
    return jpaQueryFactory.selectFrom(payment)
      .where(
        payment.id.enterpriseId.eq(enterpriseId)
          .and(payment.subscriptionInvoiceId.eq(invoiceId))
          .and(payment.resultCode.eq("3001"))
      )
      .fetchFirst()
  }

  fun findAllBySubscriptionId(
    enterpriseId: String,
    subscriptionId: String,
  ): List<PaymentEntity> {
    return jpaQueryFactory.selectFrom(payment)
      .where(
        payment.id.enterpriseId.eq(enterpriseId)
          .and(payment.subscriptionId.eq(subscriptionId))
      )
      .fetch()
  }

  fun findAllByMemberId(
    enterpriseId: String,
    memberId: String
  ): List<PaymentEntity> {
    return jpaQueryFactory.selectFrom(payment)
      .where(
        payment.id.enterpriseId.eq(enterpriseId)
        .and(payment.memberId.eq(memberId))
      ).fetch()
  }
}