package com.example.couponrush.service

import com.example.couponrush.domain.Coupon
import com.example.couponrush.domain.CouponIssue
import com.example.couponrush.repository.CouponIssueRepository
import com.example.couponrush.repository.CouponRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CouponService(
    val couponRepository: CouponRepository,
    val couponIssueRepository: CouponIssueRepository
) {
    @Transactional
    fun issueCoupon(couponId: Long, userId: String) {
        //1.중복 발급 체크
        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw IllegalStateException("이미 발급받은 쿠폰입니다.")
        }

        //2. 쿠폰조회
        val coupon = couponRepository.findById(couponId)
            .orElseThrow { IllegalArgumentException("존재하지 않는 쿠폰입니다.") }

        //3. 발급 가능 여부 확인 및 발급
        coupon.issue()

        //4. 발급 내역 저장
        val couponIssue = CouponIssue(
            couponId = couponId,
            userId = userId
        )
        couponIssueRepository.save(couponIssue)
    }

}