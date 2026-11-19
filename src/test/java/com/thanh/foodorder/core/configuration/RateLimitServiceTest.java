package com.thanh.foodorder.core.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    @DisplayName("Cho phép các request trong giới hạn capacity và chặn request vượt quá")
    void testRateLimitConsumption() {
        String testKey = "TEST_ACTION:192.168.1.100";
        int capacity = 5;
        int refillTokens = 5;
        int durationSeconds = 60;

        // 5 request đầu tiên phải thành công
        for (int i = 1; i <= capacity; i++) {
            boolean allowed = rateLimitService.tryConsume(testKey, capacity, refillTokens, durationSeconds);
            assertTrue(allowed, "Request thứ " + i + " phải được cho phép");
        }

        // Request thứ 6 vượt quá capacity phải bị chặn ngay lập tức
        boolean rejected = rateLimitService.tryConsume(testKey, capacity, refillTokens, durationSeconds);
        assertFalse(rejected, "Request thứ 6 phải bị chặn (Rate Limit exceeded)");
    }

    @Test
    @DisplayName("Các IP khác nhau có bucket độc lập nhau")
    void testDifferentIpsHaveIsolatedBuckets() {
        String ip1Key = "LOGIN:10.0.0.1";
        String ip2Key = "LOGIN:10.0.0.2";

        // Dùng hết 2 token của IP1
        assertTrue(rateLimitService.tryConsume(ip1Key, 2, 2, 60));
        assertTrue(rateLimitService.tryConsume(ip1Key, 2, 2, 60));
        assertFalse(rateLimitService.tryConsume(ip1Key, 2, 2, 60)); // IP1 bị chặn

        // IP2 vẫn còn token và thực hiện bình thường
        assertTrue(rateLimitService.tryConsume(ip2Key, 2, 2, 60), "IP2 không bị ảnh hưởng bởi IP1");
    }
}
