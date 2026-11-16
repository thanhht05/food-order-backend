package com.thanh.foodorder.core.configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

/**
 * Service quản lý Token Bucket in-memory bằng Caffeine Cache.
 * Tự động xóa bucket không còn hoạt động sau 10 phút để tránh rò rỉ RAM (Memory Leak).
 */
@Service
public class RateLimitService {

    // Cache lưu Bucket theo key dạng "{action}:{clientIp}"
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100_000)
            .build();

    /**
     * Kiểm tra và tiêu thụ 1 token cho client.
     *
     * @param key               Định danh duy nhất (ví dụ: "LOGIN:127.0.0.1")
     * @param capacity          Dung lượng bucket tối đa
     * @param refillTokens      Số token nạp lại
     * @param durationInSeconds Chu kỳ nạp token (giây)
     * @return true nếu còn token (cho phép request), false nếu hết token (bị chặn)
     */
    public boolean tryConsume(String key, int capacity, int refillTokens, int durationInSeconds) {
        Bucket bucket = bucketCache.get(key, k -> createNewBucket(capacity, refillTokens, durationInSeconds));
        return bucket != null && bucket.tryConsume(1);
    }

    private Bucket createNewBucket(int capacity, int refillTokens, int durationInSeconds) {
        Refill refill = Refill.intervally(refillTokens, Duration.ofSeconds(durationInSeconds));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
