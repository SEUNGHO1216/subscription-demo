package com.seaotter.presentation.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
  @Bean
  fun openApi(): OpenAPI {


    return OpenAPI()
      .info(configurationInfo())
  }

  private fun configurationInfo(): Info {
    return Info()
      .title("Subscription App Doc")
      .description("Subscription App Doc")

      .version("1.0.0")
  }
}