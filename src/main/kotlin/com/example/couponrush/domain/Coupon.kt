package com.example.couponrush.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "coupons")
class Coupon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val totalQuantity: Int,
    @Column(nullable = false)
    var issuedQuantity: Int = 0,
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    fun canIssue (): Boolean {
        return totalQuantity > issuedQuantity
    }
    fun issue (){
        if (!canIssue()) {
            throw IllegalStateException("쿠폰이 모두 소진되었습니다.")
        }
        issuedQuantity++
    }


}