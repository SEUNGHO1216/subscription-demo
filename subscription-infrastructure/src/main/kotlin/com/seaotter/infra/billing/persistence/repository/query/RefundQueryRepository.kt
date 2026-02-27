package com.seaotter.infra.billing.persistence.repository.query

import com.querydsl.jpa.impl.JPAQueryFactory
import com.seaotter.infra.billing.persistence.entity.QRefundEntity
import com.seaotter.infra.billing.persistence.entity.RefundEntity
import org.springframework.stereotype.Repository

@Repository
class RefundQueryRepository(
  private val jpaQueryFactory: JPAQueryFactory,
  private val refund: QRefundEntity = QRefundEntity.refundEntity
) {

  fun findAllByMemberId(
    enterpriseId: String,
    memberId: String
  ): List<RefundEntity> {
    return jpaQueryFactory.selectFrom(refund)
      .where(
        refund.id.enterpriseId.eq(enterpriseId)
          .and(refund.memberId.eq(memberId))
      ).fetch()
  }

  fun findAllByPaymentId(
    enterpriseId: String,
    paymentId: String
  ): List<RefundEntity> {
    return jpaQueryFactory.selectFrom(refund)
      .where(
        refund.id.enterpriseId.eq(enterpriseId)
          .and(refund.paymentId.eq(paymentId))
      ).fetch()  }
}