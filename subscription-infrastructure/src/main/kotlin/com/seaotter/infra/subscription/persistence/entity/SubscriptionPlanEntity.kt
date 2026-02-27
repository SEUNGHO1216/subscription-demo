package com.seaotter.infra.subscription.persistence.entity

import com.seaotter.core.domain.common.BaseInfo
import com.seaotter.core.domain.subscription.BillingType
import com.seaotter.core.domain.subscription.SubscriptionBillingPolicy
import com.seaotter.core.domain.subscription.SubscriptionPlan
import com.seaotter.core.domain.subscription.SubscriptionPricing
import com.seaotter.infra.common.BaseInfoEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionPlanEntityId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal

@Table(
  name = "subscription_plan",
  indexes = [
    Index(name = "idx_enterprise_product", columnList = "enterprise_id, subscription_product_id"),
    Index(name = "idx_enterprise_store_product", columnList = "enterprise_id, store_id, subscription_product_id"),
  ]
)
@Entity
class SubscriptionPlanEntity(
  @EmbeddedId
  val id: SubscriptionPlanEntityId, // version 정보도 들어가자
  val storeId: String,
  val subscriptionProductId: String,
  var version: Int,
  var name: String,
  var grade: String? = null,
  var description: String? = null,
  val defaultPrice: BigDecimal = BigDecimal(0),
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(value = SqlTypes.VARCHAR)
  val billingType: BillingType,
  @Column(precision = 19, scale = 2)
  val price: BigDecimal,
  @Column(name = "price_info", columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  var priceInfo: SubscriptionPricing,
  @Column(name = "billing_policy", columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  var billingPolicy: SubscriptionBillingPolicy,
  var isActive: Boolean,
): BaseInfoEntity() {

  fun toDomain(): SubscriptionPlan {
    return SubscriptionPlan(
      enterpriseId = this.id.enterpriseId,
      id = this.id.subscriptionPlanId,
      storeId = this.storeId,
      subscriptionProductId = this.subscriptionProductId,
      version = this.version,
      name = this.name,
      grade = this.grade,
      description = this.description,
      priceInfo = this.priceInfo,
      billingPolicy = this.billingPolicy,
      isActive = this.isActive,
      baseInfo = BaseInfo(
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        deletedAt = this.deletedAt,
        createdBy = this.createdBy,
        updatedBy = this.updatedBy,
        deletedBy = this.deletedBy,
      )
    )
  }

  companion object {
    fun toEntity(subscriptionPlan: SubscriptionPlan): SubscriptionPlanEntity {
      val entity = SubscriptionPlanEntity(
        id = SubscriptionPlanEntityId(
          subscriptionPlan.enterpriseId,
          subscriptionPlan.id
        ),
        storeId = subscriptionPlan.storeId,
        subscriptionProductId = subscriptionPlan.subscriptionProductId,
        version = subscriptionPlan.version,
        name = subscriptionPlan.name,
        grade = subscriptionPlan.grade,
        price = subscriptionPlan.priceInfo.defaultPrice,
        description = subscriptionPlan.description,
        defaultPrice = subscriptionPlan.priceInfo.defaultPrice,
        billingType = subscriptionPlan.billingPolicy.billingType,
        priceInfo = subscriptionPlan.priceInfo,
        billingPolicy = subscriptionPlan.billingPolicy,
        isActive = subscriptionPlan.isActive,
      )
      entity.deletedAt = subscriptionPlan.baseInfo?.deletedAt
      entity.deletedBy = subscriptionPlan.baseInfo?.deletedBy
      return entity
    }
  }

}