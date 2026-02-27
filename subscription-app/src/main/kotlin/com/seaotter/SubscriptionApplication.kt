package com.seaotter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class SubscriptionApplication

fun main(args: Array<String>) {
  runApplication<SubscriptionApplication>(*args)
}