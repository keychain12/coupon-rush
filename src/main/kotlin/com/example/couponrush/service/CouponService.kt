package com.example.couponrush.service

import com.example.couponrush.CouponIssueWriter
import com.example.couponrush.domain.Coupon
import com.example.couponrush.domain.CouponIssue
import com.example.couponrush.repository.CouponIssueRepository
import com.example.couponrush.repository.CouponRepository
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Duration
import javax.print.PrintService

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val couponIssueRepository: CouponIssueRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val couponIssueWriter: CouponIssueWriter
) {
    /*
    @Transactional
    fun issueCoupon(couponId: Long, userId: String) { //v1,2
        //1.중복 발급 체크
        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw IllegalStateException("이미 발급받은 쿠폰입니다.")
        }

        //2. 쿠폰조회 (Lock 걸기)
        val coupon = couponRepository.findByIdWithLock(couponId)
            ?: throw IllegalArgumentException("존재하지 않는 쿠폰입니다")

        //3. 발급 가능 여부 확인 및 발급
        coupon.issue()

        //4. 발급 내역 저장
        val couponIssue = CouponIssue(
            couponId = couponId,
            userId = userId
        )
        couponIssueRepository.save(couponIssue)
    }*/

    fun issueCoupon(couponId: Long, userId: String){ //v3 레디스사용
        val stockKey = "coupon:$couponId:stock"
        val issueKey = "coupon:$couponId:issued:$userId"

        // 레디스로 중복체크
        val alreadyIssued = redisTemplate.opsForValue()
            .setIfAbsent(issueKey, "1", Duration.ofDays(7))

        if (alreadyIssued == false) {
            throw IllegalStateException("이미 발급받은 쿠폰입니다.")
        }

        // Lua 스크립트로 재고 차감
        val script = """
        local stock = redis.call('GET', KEYS[1])
        if stock == false or tonumber(stock) <= 0 then
            return -1
        end
        return redis.call('DECR', KEYS[1])
    """.trimIndent()


        //레디스로 재고차감
        val remaining = redisTemplate.execute(
            org.springframework.data.redis.core.script.RedisScript.of(script, Long::class.java),
            listOf(stockKey)
        )

        if (remaining == null || remaining < 0) {
            //실패시 롤백
            redisTemplate.delete(issueKey)
            throw IllegalStateException("쿠폰이 모두 소진되었습니다.")
        }

        //비동기 DB로 저장
        couponIssueWriter.saveToDBAsync(couponId, userId)
    }


}