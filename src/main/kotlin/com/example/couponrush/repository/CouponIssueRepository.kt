package com.example.couponrush.repository

import com.example.couponrush.domain.CouponIssue
import org.springframework.data.jpa.repository.JpaRepository

interface CouponIssueRepository : JpaRepository<CouponIssue, Long> {
    fun existsByCouponIdAndUserId(couponId: Long, userId: String): Boolean
}