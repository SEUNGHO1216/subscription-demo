package com.seaotter.infra.billing.pg.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.Key
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class SignatureGenerator {
  companion object {
    val log = LoggerFactory.getLogger(this::class.java)

    fun hashWithSHA256(data: String): String {
      val encodedData: String? = null
      try {
        // MessageDigest 인스턴스를 SHA-256으로 초기화
        val digest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        // 문자열을 바이트 배열로 변환하고 해시 계산
        digest.update(data.toByteArray())
        val encodedHash = digest.digest()

        // 바이트 배열을 16진수 문자열로 변환하여 반환
        return _bytesToHex(encodedHash)
      } catch (e: Exception) {
        log.error("암호화 에러 : {}", e.message)
        throw RuntimeException("암호화 에러 >> " + e)
      }
    }

    fun encodeWithAES(data: String, key: Key?): String {
      try {
        // AES/ECB/PKCS5padding 알고리즘 적용
        val instance = Cipher.getInstance("AES/ECB/PKCS5padding")
        instance.init(Cipher.ENCRYPT_MODE, key) // new IvParameterSpec 필요?? -> ECB 모드에서는 않는다.
        // 암호화 실행 중 ..
        val bytes = instance.doFinal(data.toString().toByteArray(charset("UTF-8")))

        // 암호 결과 인코딩 HEX encoding
        return _bytesToHex(bytes)
      } catch (e: java.lang.Exception) {
        log.error("encodeWithAES 암호화 에러 : {}", e.message)
        throw java.lang.RuntimeException("암호화 에러 >> " + e)
      }
    }

    fun getTargetDataForBilling(
      cardNoValue: String,
      expYear: String,
      expMonth: String,
      idNo: String?,  // 생년월일로 가자
      cardPw: String
    ): String {
      // 암호화 타겟 데이터 생성
      val targetData = StringBuffer()
      targetData.append("CardNo=").append(cardNoValue).append("&")
      targetData.append("ExpYear=").append(expYear).append("&")
      targetData.append("ExpMonth=").append(expMonth).append("&")
      if (idNo != null) {
        targetData.append("IDNo=").append(idNo).append("&")
      }
      targetData.append("CardPw=").append(cardPw)
      return targetData.toString()
    }

    fun getMerchantKeyForEncoding(merchantKey: String): Key {
      val encodeKey = merchantKey.substring(0, 16)
      // 암호 key : mid 에서 부여된 merchantKey 앞 16자리
      return SecretKeySpec(encodeKey.toByteArray(), "AES")
    }

    // 바이트 배열을 16진수 문자열로 변환하는 헬퍼 메소드
    private fun _bytesToHex(hash: ByteArray): String {
      val hexString = StringBuffer(2 * hash.size)
      for (b in hash) {
        val hex = Integer.toHexString(0xff and b.toInt())
        if (hex.length == 1) {
          hexString.append('0')
        }
        hexString.append(hex)
      }
      return hexString.toString()
    }
  }


}