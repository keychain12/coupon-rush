package com.example.couponrush.repository

import com.example.couponrush.domain.Coupon
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface CouponRepository : JpaRepository<Coupon, Long> {

    //비관적 락 추가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    fun findByIdWithLock(id: Long): Coupon?

    @Modifying
    @Query("UPDATE Coupon c SET c.issuedQuantity = c.issuedQuantity + 1 WHERE c.id = :id")
    fun increaseIssuedQuantity(id: Long): Int
}