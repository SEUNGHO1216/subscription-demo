package com.seaotter.infra.billing.pg

import com.seaotter.core.application.billing.dto.BillingKeyRegisterRequest
import com.seaotter.core.application.billing.dto.BillingKeyRegisterResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse
import com.seaotter.infra.billing.feign.NicePaymentBillingRegisterRequest
import com.seaotter.infra.billing.feign.NicePaymentCancelProcessRequest
import com.seaotter.infra.billing.feign.NicePaymentFeign
import com.seaotter.infra.billing.feign.NicePaymentPayRequest
import com.seaotter.infra.billing.pg.util.DateUtil
import com.seaotter.infra.billing.pg.util.SignatureGenerator
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class NicePaymentBillingService(
  private val nicePaymentFeign: NicePaymentFeign
) {
  val log = LoggerFactory.getLogger(this::class.java)

  val merchantId: String ="nictest26m"
  val merchantKey: String ="yEALxi09avQoCtEj75hocQyZX+j9fsUk60B3GB7MQksLCnwLNTZLEGe7nedF5+qaJZZeNovXB4s9qPuA6zzqWQ=="

  fun registerBillingKey(billingKeyRegisterRequest: BillingKeyRegisterRequest): BillingKeyRegisterResponse {
    log.info("registerBillingKey > billingKeyRegisterRequest: {}", billingKeyRegisterRequest)
    val plainTextForBillKey = SignatureGenerator.getTargetDataForBilling(
      billingKeyRegisterRequest.cardNumber,
      billingKeyRegisterRequest.expireYear,
      billingKeyRegisterRequest.expireMonth,
      billingKeyRegisterRequest.birthday.substring(2),
      billingKeyRegisterRequest.cardPassword
    )
    val createdDate = DateUtil.getNow_YYYYMMDDHHMMSS()
    log.info("mid: {}", merchantId)
    try {
      return NicePaymentBillingRegisterRequest(
        mid = merchantId,
        ediDate = createdDate,
        encData = SignatureGenerator.encodeWithAES(
          plainTextForBillKey,
          SignatureGenerator.getMerchantKeyForEncoding(merchantKey)
        ),
        signData = this._shaEncodeForBillingKeyRegister(createdDate, billingKeyRegisterRequest.memberId),
        billingRegisterRequest = billingKeyRegisterRequest
      ).let {
        val result = nicePaymentFeign.registerBillKey(it).also {
          if(it.ResultCode != "F100") {
            log.error("[register fail > ${it.ResultCode}] ${it.ResultMsg}")
            throw RuntimeException("[${it.ResultCode}] ${it.ResultMsg}")
          }
        }
        BillingKeyRegisterResponse(
          result.BID,
          result.CardCode,
          result.CardName,
          result.CardCl
        )
      }
    }catch (e: RuntimeException) {
      throw RuntimeException("[빌링키발급 네트워크 에러] ${e.message}")
    }

  }


  fun pay(billingKeyPayRequest: BillingKeyPayRequest): BillingKeyPayResponse {
    log.info("pay > billingKeyPayRequest: {}", billingKeyPayRequest)
    val createdDate = DateUtil.getNow_YYYYMMDDHHMMSS()
    try {
      return NicePaymentPayRequest(
        mid = merchantId,
        tid = _generateTID(createdDate),
        createdDate = createdDate,
        signData = this._shaEncodeForBillingKeyPay(
          createdDate,
          billingKeyPayRequest.orderId!!,
          billingKeyPayRequest.amount,
          billingKeyPayRequest.billKey!!
        ),
        billingPayRequest = billingKeyPayRequest
      ).let {
        val result = nicePaymentFeign.payBillKey(it).also {
          if(it.ResultCode != "3001") {
            log.error("pay fail > [${it.ResultCode}] ${it.ResultMsg}")
          }
        }
        result.toDomainPayResponse(billingKeyPayRequest.billKey!!)
      }
    } catch (e: RuntimeException) {
      throw RuntimeException("[빌링결제승인 네트워크 에러] ${e.message}")
    }
  }

  fun payCancel(payCancelRequest: BillingKeyPayCancelRequest): BillingKeyPayCancelResponse {
    log.info("payCancel > payCancelRequest: {}", payCancelRequest)
    val createdDate = DateUtil.getNow_YYYYMMDDHHMMSS()
    try {
      return NicePaymentCancelProcessRequest(
        mid = merchantId,
        createdDate = createdDate,
        signData = this._shaEncodeForBillingKeyCancel(createdDate, payCancelRequest.amount),
        billingPayCancelRequest = payCancelRequest
      ).let {
        val result = nicePaymentFeign.cancelBilling(it).also {
          if(it.ResultCode != "2001") {
            log.error("pay cancel fail >[${it.ResultCode}] ${it.ResultMsg}")
          }
        }
        result.toDomainCancelResponse()
      }
    } catch (e: RuntimeException) {
      throw RuntimeException("[빌링결제취소 네트워크 에러] ${e.message}")
    }
  }

  private fun _generateTID(createdDate: String): String {
    // "01 -> 카드 결제 , 16 -> 빌링 결제" 즉 카드 빌링 결제를 의미하는 것이며 고정값으로 둬도 괜찮다.
    return StringBuffer().append(merchantId).append("0116").append(createdDate.substring(2))
      .append(RandomStringUtils.randomAlphanumeric(4)).toString()
  }

  private fun _shaEncodeForBillingKeyRegister(createdDate: String, memberId: String): String {

    return StringBuffer().apply {
      append(merchantId).append(createdDate).append(memberId).append(merchantKey).toString()
    }.toString().run {
      SignatureGenerator.hashWithSHA256(this)
    }
  }

  private fun _shaEncodeForBillingKeyPay(
    createdDate: String,
    orderId: String,
    amount: Long,
    billKey: String
  ): String {
    return StringBuffer().apply {
      append(merchantId).append(createdDate).append(orderId)
        .append(amount).append(billKey).append(merchantKey).toString()
    }.toString().run {
      SignatureGenerator.hashWithSHA256(this)
    }
  }

  private fun _shaEncodeForBillingKeyCancel(
    createdDate: String,
    cancelAmount: Long,
  ): String {
    return StringBuffer().apply {
      append(merchantId).append(cancelAmount)
        .append(createdDate).append(merchantKey).toString()
    }.toString().run {
      SignatureGenerator.hashWithSHA256(this)
    }
  }
}