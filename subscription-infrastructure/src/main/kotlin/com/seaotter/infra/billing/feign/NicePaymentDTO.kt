package com.seaotter.infra.billing.feign

import com.seaotter.core.application.billing.dto.BillingKeyRegisterRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse

data class NicePaymentBillingRegisterRequest(
  var MID: String, // 상점 ID
  var EdiDate: String, // 전문생성일시 (YYYYMMDDHHMISS)
  var Moid: String,
  var EncData: String,
  var SignData: String, // 위변조 검증 Data
  var BuyerName: String,
  var BuyerTel: String,
  var CharSet: String, // 응답파라메터 인코딩 방식
  var EdiType: String, // 응답전문 유형 (default(미설정): JSON / KV(설정): Key=Value형식 응답)
) {
  constructor(
    mid: String,
    ediDate: String,
    encData: String,
    signData: String,
    billingRegisterRequest: BillingKeyRegisterRequest
  ) : this(
    MID = mid,
    EdiDate = ediDate,
    Moid = billingRegisterRequest.memberId,
    EncData = encData,
    SignData = signData,
    BuyerName = billingRegisterRequest.username,
    BuyerTel = billingRegisterRequest.telephone,
    CharSet = "euc-kr",
    EdiType = "JSON"
  )
}

//data class NicePaymentBillingRegisterRequest(
//  @field:JsonProperty("MID")
//  var mid: String, // 상점 ID
//  @field:JsonProperty("EdiDate")
//  var ediDate: String, // 전문생성일시 (YYYYMMDDHHMISS)
//  @field:JsonProperty("Moid")
//  var moid: String,
//  @field:JsonProperty("EncData")
//  var encData: String,
//  @field:JsonProperty("SignData")
//  var signData: String, // 위변조 검증 Data
//  @field:JsonProperty("BuyerName")
//  var buyerName: String,
//  @field:JsonProperty("BuyerTel")
//  var buyerTel: String,
//  @field:JsonProperty("CharSet")
//  var charSet: String, // 응답파라메터 인코딩 방식
//  @field:JsonProperty("EdiType")
//  var ediType: String, // 응답전문 유형 (default(미설정): JSON / KV(설정): Key=Value형식 응답)
//) {
//  constructor(
//    mid: String,
//    ediDate: String,
//    encData: String,
//    signData: String,
//    billingRegisterRequest: BillingKeyRegisterRequest
//  ) : this(
//    mid = mid,
//    ediDate = ediDate,
//    moid = billingRegisterRequest.memberId,
//    encData = encData,
//    signData = signData,
//    buyerName = billingRegisterRequest.username,
//    buyerTel = billingRegisterRequest.telephone,
//    charSet = "euc-kr",
//    ediType = "JSON"
//  )
//
//  fun toMap(): Map<String, String> {
//    return mapOf(
//      "MID" to mid,
//      "EdiDate" to ediDate,
//      "Moid" to moid,
//      "EncData" to encData,
//      "SignData" to signData,
//      "BuyerName" to buyerName,
//      "BuyerTel" to buyerTel,
//      "CharSet" to charSet,
//      "EdiType" to ediType,
//    )
//  }
//}

data class NicePaymentBillingRegisterResponse(
  val ResultCode: String, // F100 만 성공
  val ResultMsg: String,
  val TID: String, // 거래번호, 인증결과로 받은 TxTid 파라미터 설정
  val BID: String, // 빌키
  val AuthDate: String,
  val CardCode: String, // 카드사 코드
  val CardName: String,
  val CardCl: String, // 0 : 신용카드 , 1 : 체크카드
  val AcquCardCode: String, // 매입사 코드
  val AcquCardName: String,
)

data class NicePaymentPayRequest(
  var BID: String, // billing key
  var MID: String, // 상점 ID
  var TID: String, // 거래번호, 인증결과로 받은 TxTid 파라미터 설정
  var EdiDate: String, // 전문생성일시 (YYYYMMDDHHMISS)
  var Moid: String,
  var Amt: String, // 결제금액
  var GoodsName: String,
  var SignData: String, // 위변조 검증 Data
  var CardInterest: String, // 가맹점 분담 무이자 사용 여부 (0: 사용안함(o), 1: 사용)
  var CardQuota: String, // 할부개월 (00: 일시불)
  var CharSet: String, // 응답파라메터 인코딩 방식
  var EdiType: String, // 응답전문 유형 (default(미설정): JSON / KV(설정): Key=Value형식 응답)
) {
  constructor(
    mid: String,
    tid: String,
    createdDate: String,
    signData: String,
    billingPayRequest: BillingKeyPayRequest
  ): this(
    BID = requireNotNull(billingPayRequest.billKey) { "BillingKey is null" },
    MID = mid,
    TID = tid,
    EdiDate = createdDate,
    Moid = requireNotNull(billingPayRequest.orderId) { "OrderId is null" },
    Amt = billingPayRequest.amount.toString(),
    GoodsName = billingPayRequest.goodsName,
    SignData = signData,
    CardInterest = "0",
    CardQuota = String.format("%02d",billingPayRequest.installment),
    CharSet = "utf-8",
    EdiType = "JSON",
  )
}

data class NicePaymentPayResponse(
  val ResultCode: String, // 결제 결과 코드 (결제수단별 성공코드)신용카드: 3001, 계좌이체: 4000, 가상계좌: 4100, 휴대폰: A000, SSG은행계좌: 0000, SSG복합결제: 0000, CMS계좌간편결제: 0000
  val ResultMsg: String, // 결제 결과 메시지
  val TID: String,
  val Moid: String, // 상점 주문번호
  val Amt: String, // 결제금액
  val AuthCode: String, // 승인번호 (신용카드, 계좌이체, 휴대폰)
  val AuthDate: String, // 승인날짜 (YYMMDDHHMMSS)
  val AcquCardCode: String,
  val AcquCardName: String,

// 신용카드 response
  val CardNo: String,
  val CardCode: String, // 결제 카드사 코드 (issuer)
  val CardName: String, // 결제 카드사명 (issuerName)
  val CardQuota: String, // 00 (일시불), 03(3개월 할부)
  val CardCl: String, // 카드 구분 (0: 신용, 1: 체크)
  val CardInterest: String,
  val CcPartCl: String, // 부분취소 가능 여부 (0: 불가능, 1: 가능)
) {
  fun toDomainPayResponse(billingKey: String): BillingKeyPayResponse {
    return BillingKeyPayResponse(
      billingKey = billingKey,
      orderId = Moid,
      amount = Amt.toLong(),
      resultCode = ResultCode,
      resultMsg = ResultMsg,
      tid = TID,
      authCode = AuthCode,
      authDate = AuthDate,
      cardCode = CardCode,
      cardName = CardName,
      cardNumber = CardNo,
      installment = CardQuota.toInt(),
      isCheckCard = CardCl != "0",
      canPartiallyCancel = CcPartCl != "0",
    )
  }
}

data class NicePaymentCancelProcessRequest(
  var TID: String, // 거래번호, 인증결과로 받은 TxTid 파라미터 설정
  var MID: String, // 상점 ID
  var Moid: String,
  var CancelAmt: String, // 취소금액
  var CancelMsg: String, // 취소메시지
  var PartialCancelCode: String, // 부분취소 여부(전체취소 : 0 / 부분취소 : 1)
  var EdiDate: String, // 전문생성일시 (YYYYMMDDHHMISS)
  var SignData: String, // 위변조 검증 Data
  var CharSet: String, // 응답파라메터 인코딩 방식
  var EdiType: String, // 응답전문 유형 (default(미설정): JSON / KV(설정): Key=Value형식 응답)
) {
  constructor(
    mid: String,
    createdDate: String,
    signData: String,
    billingPayCancelRequest: BillingKeyPayCancelRequest
  ) : this(
    TID = requireNotNull(billingPayCancelRequest.tid) { "Tid is null" },
    MID = mid,
    Moid = billingPayCancelRequest.orderId,
    CancelAmt = billingPayCancelRequest.amount.toString(),
    CancelMsg = billingPayCancelRequest.cancelMessage,
    PartialCancelCode = if(billingPayCancelRequest.isPartiallyCancel) "1" else "0",
    EdiDate = createdDate,
    SignData = signData,
    CharSet = "utf-8",
    EdiType = "JSON",
  )
}

data class NicePaymentCancelProcessResponse(
  val ResultCode: String, // 취소 결과 코드 (성공 : 2001 / 그외 실패)
  val ResultMsg: String, // 취소 결과 메시지
  val ErrorCD: String, // 에러코드
  val ErrorMsg: String, // 에러메시지
  val CancelAmt: String,
  val MID: String, // 상점 ID
  val Moid: String, // 상점 주문번호
  val Signature: String, // 위변조 검증 Data
  val PayMethod: String,
  val TID: String,
  val CancelDate: String, // YYYYMMDD
  val CancelTime: String, // HHmmss
  val CancelNum: String, // 취호 번호
  val RemainAmt: String, // 취소 후 잔액
// 신용카드 response
  val ClickpayCl: String, // 간편결제 서비스명 - 6: SKPAY / 8: SAMSUNGPAY (구버전 사용 시) / 15: PAYCO / 16: KAKAOPAY / 20: NAVERPAY / 21: SAMSUNGPAY
) {
  fun toDomainCancelResponse(): BillingKeyPayCancelResponse {
    return BillingKeyPayCancelResponse(
      orderId = Moid,
      canceledAmount = CancelAmt.toLong(),
      resultCode = ResultCode,
      resultMsg = ResultMsg,
      tid = TID,
      cancelTime = CancelTime,
      cancelDate = CancelDate,
      remainAmount = RemainAmt.toLong(),
    )
  }
}