package com.seaotter.infra.billing.persistence.entity

import com.seaotter.core.domain.billing.BillingKey
import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.core.domain.common.BaseInfo
import com.seaotter.infra.billing.persistence.entity.id.BillingKeyEntityId
import com.seaotter.infra.common.BaseInfoEntity
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes


@Table(
  name = "billing_key",
  indexes = [
    Index(
      name = "idx_enterprise_member",
      columnList = "enterprise_id, member_id"
    )
  ]
)
@Entity
class BillingKeyEntity(
  @EmbeddedId
  val id: BillingKeyEntityId,
  val memberId: String,
  val billKey: String,
  val cardCompanyCode: String,
  val cardCompanyName: String,
  val isCheckCard: Boolean,
  val cardNumber: String,
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(value = SqlTypes.VARCHAR)
  val van: VanCompany = VanCompany.NICE_PAYMENTS,
  var isActive: Boolean = true,
): BaseInfoEntity() {

  fun toDomain(): BillingKey {
    return BillingKey(
      enterpriseId = this.id.enterpriseId,
      id = this.id.billingKeyId,
      memberId = memberId,
      billKey = billKey,
      cardCompanyCode = this.cardCompanyCode,
      cardCompanyName = this.cardCompanyName,
      isCheckCard = this.isCheckCard,
      cardNumber = this.cardNumber,
      van = this.van,
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
    fun toEntity(domain: BillingKey): BillingKeyEntity {
      val entity = BillingKeyEntity(
        id = BillingKeyEntityId(
          domain.enterpriseId,
          domain.id
        ),
        memberId = domain.memberId,
        billKey = domain.billKey,
        cardCompanyCode = domain.cardCompanyCode,
        cardCompanyName = domain.cardCompanyName,
        isCheckCard = domain.isCheckCard,
        cardNumber = domain.cardNumber,
        van = domain.van,
        isActive = domain.isActive,
      )
      entity.deletedAt =domain.baseInfo?.deletedAt
      entity.deletedBy = domain.baseInfo?.deletedBy
      return entity
    }
  }

}