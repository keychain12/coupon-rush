package com.example.couponrush

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class CouponRushApplication

fun main(args: Array<String>) {
    runApplication<CouponRushApplication>(*args)
}
