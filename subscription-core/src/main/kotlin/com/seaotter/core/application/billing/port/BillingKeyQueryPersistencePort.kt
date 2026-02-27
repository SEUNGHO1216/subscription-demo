package com.seaotter.core.application.billing.port

import com.seaotter.core.domain.billing.BillingKey


interface BillingKeyQueryPersistencePort {

  fun findById(enterpriseId: String, id: String): BillingKey?
  fun findAllByMemberId(enterpriseId: String, memberId: String): List<BillingKey>
  fun findAllByIdList(enterpriseId: String, idList: List<String>): List<BillingKey>
}