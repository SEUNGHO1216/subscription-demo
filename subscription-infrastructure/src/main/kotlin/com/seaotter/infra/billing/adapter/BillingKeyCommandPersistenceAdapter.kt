package com.seaotter.infra.billing.adapter

import com.seaotter.core.application.billing.port.BillingKeyCommandPersistencePort
import com.seaotter.core.domain.billing.BillingKey
import com.seaotter.infra.billing.persistence.entity.BillingKeyEntity
import com.seaotter.infra.billing.persistence.entity.id.BillingKeyEntityId
import com.seaotter.infra.billing.persistence.repository.BillingKeyCommandRepository
import org.springframework.stereotype.Component

@Component
class BillingKeyCommandPersistenceAdapter(
  private val billingKeyCommandRepository: BillingKeyCommandRepository
): BillingKeyCommandPersistencePort {

  override fun save(billingKey: BillingKey): BillingKey {

    return billingKeyCommandRepository.save(
      BillingKeyEntity.toEntity(billingKey)
    ).toDomain()
  }

  override fun saveAll(billingKeys: List<BillingKey>): List<BillingKey> {
    return billingKeyCommandRepository.saveAll(
      billingKeys.map { BillingKeyEntity.toEntity(it) }
    ).map { it.toDomain() }
  }

  override fun deleteById(enterpriseId: String, id: String) {
    billingKeyCommandRepository.deleteById(BillingKeyEntityId(enterpriseId, id))
  }
}