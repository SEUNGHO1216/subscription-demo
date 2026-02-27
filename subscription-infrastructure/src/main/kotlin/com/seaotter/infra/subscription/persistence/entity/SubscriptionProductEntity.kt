package com.seaotter.infra.subscription.persistence.entity

import com.seaotter.core.domain.common.BaseInfo
import com.seaotter.core.domain.subscription.SubscriptionProduct
import com.seaotter.infra.common.BaseInfoEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionProductEntityId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Table(
  name = "subscription_product",
  indexes = [
    Index(name = "idx_enterprise_store", columnList = "enterprise_id, store_id"),
  ]
)
@Entity
class SubscriptionProductEntity(
  @EmbeddedId
  val id: SubscriptionProductEntityId,
  val storeId: String,
  var name: String, // gemini pro, netflix standard
  var description: String? = null,
  var isActive: Boolean = false,
): BaseInfoEntity() {

  fun toDomain(): SubscriptionProduct {
    return SubscriptionProduct(
      id = this.id.subscriptionProductId,
      enterpriseId = this.id.enterpriseId,
      storeId = this.storeId,
      name = this.name,
      description = this.description,
      isActive = this.isActive,
      baseInfo = BaseInfo(
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        deletedAt = this.deletedAt,
        createdBy = this.createdBy,
        updatedBy = this.updatedBy,
        deletedBy = this.deletedBy
      )
    )
  }

  companion object {
    fun toEntity(domain : SubscriptionProduct): SubscriptionProductEntity {
      val entity = SubscriptionProductEntity(
        id = SubscriptionProductEntityId(domain.enterpriseId, domain.id),
        storeId = domain.storeId,
        name = domain.name,
        description = domain.description,
        isActive = domain.isActive
      )
      entity.deletedAt = domain.baseInfo?.deletedAt
      entity.deletedBy = domain.baseInfo?.deletedBy
      return entity
    }
  }
}