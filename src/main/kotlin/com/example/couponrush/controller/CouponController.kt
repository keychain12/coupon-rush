package com.example.couponrush.controller

import com.example.couponrush.domain.CouponIssue
import com.example.couponrush.domain.dto.CouponIssueRequest
import com.example.couponrush.domain.dto.CouponIssueResponse
import com.example.couponrush.service.CouponService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/coupons")
class CouponController(
    val couponService: CouponService
){
    @PostMapping("/{couponId}/issue")
    fun issueCoupon(
        @PathVariable couponId: Long,
        @RequestBody request: CouponIssueRequest
    ): ResponseEntity<CouponIssueResponse> {
        return try {
            couponService.issueCoupon(couponId, request.userId)
            ResponseEntity.ok(CouponIssueResponse(true, "쿠폰발급완료"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(CouponIssueResponse(false, e.message ?: "쿠폰발급실패"))
        }

    }


}