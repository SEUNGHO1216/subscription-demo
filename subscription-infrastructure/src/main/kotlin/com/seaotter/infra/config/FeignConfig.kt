package com.seaotter.infra.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@EnableFeignClients(basePackages = ["com.seaotter.infra"])
@Configuration
class FeignConfig {
}