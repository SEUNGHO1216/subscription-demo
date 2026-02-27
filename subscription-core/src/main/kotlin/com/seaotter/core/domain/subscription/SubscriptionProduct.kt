package com.seaotter.core.domain.subscription

import com.github.f4b6a3.ulid.UlidCreator
import com.seaotter.core.domain.common.BaseInfo
import java.time.LocalDateTime
import java.util.UUID

class SubscriptionProduct(
  val enterpriseId: String,
  val id: String = UlidCreator.getUlid().toString() + UUID.randomUUID().toString().split("-")[0],
  val storeId: String,
  var name: String, // gemini pro, netflix standard
  var description: String? = null,
  var isActive: Boolean = false,
  var baseInfo: BaseInfo? = null,
) {

  fun update(
    name: String,
    description: String?,
    isActive: Boolean,
  ): SubscriptionProduct {
    this.name = name
    this.description = description
    this.isActive = isActive
    return this
  }

  fun delete(deletedBy: String?): SubscriptionProduct {
    this.baseInfo = this.baseInfo?.copy(deletedAt = LocalDateTime.now(), deletedBy = deletedBy)
    this.isActive = false
    return this
  }

  fun activate(): SubscriptionProduct {
    if (this.baseInfo?.deletedAt == null) {
      this.isActive = true
    }
    return this
  }

  fun inactivate(): SubscriptionProduct {
    this.isActive = false
    return this
  }
}