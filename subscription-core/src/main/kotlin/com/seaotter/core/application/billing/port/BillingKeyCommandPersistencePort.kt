package com.seaotter.core.application.billing.port

import com.seaotter.core.domain.billing.BillingKey

interface BillingKeyCommandPersistencePort {

  fun save(billingKey: BillingKey): BillingKey
  fun saveAll(it: List<BillingKey>): List<BillingKey>
  fun deleteById(enterpriseId: String, id: String)
}