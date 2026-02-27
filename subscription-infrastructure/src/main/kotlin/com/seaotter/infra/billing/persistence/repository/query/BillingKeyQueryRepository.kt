package com.seaotter.infra.billing.persistence.repository.query

import com.querydsl.jpa.impl.JPAQueryFactory
import com.seaotter.core.domain.billing.BillingKey
import com.seaotter.infra.billing.persistence.entity.BillingKeyEntity
import com.seaotter.infra.billing.persistence.entity.QBillingKeyEntity
import com.seaotter.infra.billing.persistence.entity.id.BillingKeyEntityId
import org.springframework.stereotype.Repository

@Repository
class BillingKeyQueryRepository(
  private val jpaQueryFactory: JPAQueryFactory,
  private val billingKey: QBillingKeyEntity = QBillingKeyEntity.billingKeyEntity
) {

  fun findById(enterpriseId: String, id: String): BillingKeyEntity? {
    return jpaQueryFactory.selectFrom(billingKey)
      .where(
        billingKey.id.eq(BillingKeyEntityId(enterpriseId, id))
          .and(billingKey.deletedAt.isNull())
      )
      .fetchFirst()
  }

  fun findAllByMemberId(enterpriseId: String, memberId: String): List<BillingKeyEntity> {
    return jpaQueryFactory.selectFrom(billingKey)
      .where(
        billingKey.id.enterpriseId.eq(enterpriseId)
          .and(billingKey.memberId.eq(memberId))
          .and(billingKey.deletedAt.isNull())
      ).fetch()
  }
  fun findAllByIdList(enterpriseId: String, idList: List<String>): List<BillingKeyEntity> {
    val idList = idList.map { BillingKeyEntityId(enterpriseId, it) }
    return jpaQueryFactory.selectFrom(billingKey)
      .where(
        billingKey.id.`in`(idList)
          .and(billingKey.deletedAt.isNull())
      ).fetch()
  }
}