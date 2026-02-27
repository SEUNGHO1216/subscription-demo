package com.seaotter.infra.billing.feign

import feign.codec.Decoder
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import java.nio.charset.Charset

class NicePaymentFeignConfigKotlin {

  @Bean
  fun feignDecoder(): Decoder {

    val jacksonConverter = MappingJackson2HttpMessageConverter()

    // text/html + EUC-KR 도 JSON처럼 처리
    jacksonConverter.supportedMediaTypes = listOf(
      MediaType.APPLICATION_JSON,
      MediaType("text", "html", Charset.forName("EUC-KR"))
    )

    val converters = HttpMessageConverters(jacksonConverter)

    return SpringDecoder { converters }
  }

  @Bean
  fun feignLoggerLevel(): feign.Logger.Level {
    return feign.Logger.Level.FULL
  }
}
