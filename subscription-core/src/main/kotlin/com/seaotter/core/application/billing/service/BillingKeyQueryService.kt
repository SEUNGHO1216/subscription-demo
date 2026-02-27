package com.seaotter.core.application.billing.service

import com.seaotter.core.application.billing.dto.BillingKeyDTO
import com.seaotter.core.application.billing.port.BillingKeyQueryPersistencePort
import org.springframework.stereotype.Service

@Service
class BillingKeyQueryService(
  private val billingKeyQueryPersistencePort: BillingKeyQueryPersistencePort
) {

  fun findById(enterpriseId: String, id: String): BillingKeyDTO? {
    return billingKeyQueryPersistencePort.findById(enterpriseId, id)
      ?.let { BillingKeyDTO(it) }
  }

  fun findAllByMemberId(enterpriseId: String, memberId: String): List<BillingKeyDTO> {
    return billingKeyQueryPersistencePort.findAllByMemberId(enterpriseId, memberId)
      .map { BillingKeyDTO(it) }
  }
}