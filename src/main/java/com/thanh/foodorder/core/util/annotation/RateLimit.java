package com.thanh.foodorder.core.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply Rate Limiting to specific endpoints based on client IP.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum requests allowed in the refill period (Bucket capacity).
     */
    int capacity() default 10;

    /**
     * Number of refill tokens per period.
     */
    int refillTokens() default 10;

    /**
     * Refill duration in seconds (defaults to 60 seconds / 1 minute).
     */
    int durationInSeconds() default 60;
}
