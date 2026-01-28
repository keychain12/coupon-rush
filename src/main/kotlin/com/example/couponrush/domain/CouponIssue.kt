package com.example.couponrush.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(name = "coupon_issues",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_coupon_user", columnNames = ["coupon_id", "user_id"])
    ]
)

class CouponIssue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val couponId: Long,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false, updatable = false)
    val issueAt : LocalDateTime = LocalDateTime.now()
) {


}