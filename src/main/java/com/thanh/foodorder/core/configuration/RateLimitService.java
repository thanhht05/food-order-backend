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
 * In-memory Token Bucket management service powered by Caffeine Cache.
 * Automatically evicts inactive buckets after 10 minutes to prevent memory leaks.
 */
@Service
public class RateLimitService {

    // Cache storing Bucket instances by key format "{action}:{clientIp}"
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100_000)
            .build();

    /**
     * Try to consume 1 token for client.
     *
     * @param key               Unique identifier (e.g. "LOGIN:127.0.0.1")
     * @param capacity          Maximum bucket capacity
     * @param refillTokens      Number of tokens to refill
     * @param durationInSeconds Refill period in seconds
     * @return true if token was consumed (allow request), false if limit exceeded
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
