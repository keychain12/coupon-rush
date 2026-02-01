package com.example.couponrush

import com.example.couponrush.domain.CouponIssue
import com.example.couponrush.repository.CouponIssueRepository
import com.example.couponrush.repository.CouponRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponIssueWriter(
    private val couponIssueRepository: CouponIssueRepository,
    private val couponRepository: CouponRepository
) {

    @Async("taskExecutor")
    @Transactional
    fun saveToDBAsync(couponId: Long, userId: String) {
        couponIssueRepository.save(
            CouponIssue(couponId = couponId, userId = userId)
        )
        couponRepository.increaseIssuedQuantity(couponId)
    }
}