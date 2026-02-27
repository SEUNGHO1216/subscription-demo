package com.seaotter.infra.billing.adapter

import com.seaotter.core.application.billing.port.BillingKeyQueryPersistencePort
import com.seaotter.core.domain.billing.BillingKey
import com.seaotter.infra.billing.persistence.repository.query.BillingKeyQueryRepository
import org.springframework.stereotype.Component

@Component
class BillingKeyQueryPersistenceAdapter(
  private val billingKeyQueryRepository: BillingKeyQueryRepository
) : BillingKeyQueryPersistencePort {
  override fun findById(enterpriseId: String, id: String): BillingKey? {
    return billingKeyQueryRepository.findById(enterpriseId, id)?.toDomain()
  }

  override fun findAllByMemberId(enterpriseId: String, memberId: String): List<BillingKey> {
    return billingKeyQueryRepository.findAllByMemberId(enterpriseId, memberId)
      .map { it.toDomain() }
  }

  override fun findAllByIdList(
    enterpriseId: String,
    idList: List<String>
  ): List<BillingKey> {
    return billingKeyQueryRepository.findAllByIdList(enterpriseId, idList)
      .map { it.toDomain() }
  }
}