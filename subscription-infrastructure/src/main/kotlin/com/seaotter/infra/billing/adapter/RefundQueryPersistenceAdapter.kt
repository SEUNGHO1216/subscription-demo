package com.seaotter.infra.billing.adapter

import com.seaotter.core.application.billing.port.RefundQueryPersistencePort
import com.seaotter.core.domain.billing.Refund
import com.seaotter.infra.billing.persistence.repository.query.RefundQueryRepository
import org.springframework.stereotype.Component

@Component
class RefundQueryPersistenceAdapter(
  private val refundQueryRepository: RefundQueryRepository
): RefundQueryPersistencePort {
  override fun findAllByMemberId(
    enterpriseId: String,
    memberId: String
  ): List<Refund> {
    return refundQueryRepository.findAllByMemberId(enterpriseId, memberId)
      .map { it.toDomain() }
  }

  override fun findAllByPaymentId(
    enterpriseId: String,
    paymentId: String
  ): List<Refund> {
    return refundQueryRepository.findAllByPaymentId(enterpriseId, paymentId)
      .map { it.toDomain() }  }
}