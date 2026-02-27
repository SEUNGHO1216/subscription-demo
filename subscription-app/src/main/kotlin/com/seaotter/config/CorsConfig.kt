package com.seaotter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {
  @Bean
  fun corsConfigurationSource(): CorsConfigurationSource? {
    val configuration = CorsConfiguration()
    configuration.addAllowedOrigin("*") // 필요한 Origin만 허용
    configuration.addAllowedMethod("*") // 모든 HTTP 메서드 허용
    configuration.addAllowedHeader("*") // 모든 헤더 허용
    configuration.allowCredentials = true // 자격 증명 허용
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
  }
}