package com.example.couponrush.repository

import com.example.couponrush.domain.Coupon
import org.springframework.data.jpa.repository.JpaRepository

interface CouponRepository : JpaRepository<Coupon, Long> {

}