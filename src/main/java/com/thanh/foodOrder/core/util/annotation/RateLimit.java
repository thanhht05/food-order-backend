package com.thanh.foodorder.core.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation đánh dấu giới hạn tần suất gọi API (Rate Limiting) theo địa chỉ IP.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Số request tối đa được phép gọi trong chu kỳ (Dung lượng token bucket).
     */
    int capacity() default 10;

    /**
     * Số token được nạp lại trong mỗi chu kỳ.
     */
    int refillTokens() default 10;

    /**
     * Chu kỳ nạp token tính bằng giây (mặc định 60 giây = 1 phút).
     */
    int durationInSeconds() default 60;
}
