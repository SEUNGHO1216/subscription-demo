package com.seaotter.core.application.billing.service

import com.seaotter.core.domain.billing.BillingKey
import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.core.application.billing.dto.BillingKeyRegisterRequest
import com.seaotter.core.application.billing.dto.BillingKeyRegisterResponse
import com.seaotter.core.application.billing.port.BillingKeyCommandPersistencePort
import com.seaotter.core.application.billing.port.BillingKeyPgPort
import com.seaotter.core.application.billing.port.BillingKeyQueryPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BillingKeyCommandService(
  private val billingKeyCommandPersistencePort: BillingKeyCommandPersistencePort,
  private val billingKeyQueryPersistencePort: BillingKeyQueryPersistencePort,
  private val billingKeyPgPort: BillingKeyPgPort
) {

  @Transactional
  fun register(enterpriseId: String, billingKeyRegisterRequest: BillingKeyRegisterRequest): BillingKeyRegisterResponse {
    // 빌키등록
    val result = billingKeyPgPort.register(billingKeyRegisterRequest)

    BillingKey(
      enterpriseId = enterpriseId,
      memberId = billingKeyRegisterRequest.memberId,
      billKey = result.billKey,
      cardCompanyCode = result.cardCode,
      cardCompanyName = result.cardName,
      isCheckCard = result.checkOrCreditCode == "1",
      cardNumber = billingKeyRegisterRequest.cardNumber.run { _maskCardNumber(this) },
      van = billingKeyRegisterRequest.van?: getVanCompanyByEnterpriseId(enterpriseId),
    ).run { billingKeyCommandPersistencePort.save(this) }
    return result;
  }

  @Transactional
  fun delete(enterpriseId: String, idList: List<String>) {
    billingKeyQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { it.delete() }
      .also { billingKeyCommandPersistencePort.saveAll(it)}
  }


  private fun _maskCardNumber(cardNumber: String?): String {
    if (cardNumber == null) {
      return ""
    }
    return cardNumber.substring(0, 4) + "*".repeat(8) + cardNumber.substring(12)
  }

  // 일단 가라로
  private fun getVanCompanyByEnterpriseId(enterpriseId: String): VanCompany {
    return VanCompany.NICE_PAYMENTS
  }
}