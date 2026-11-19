package com.thanh.foodorder.core.configuration;

import java.io.IOException;
import java.lang.reflect.Method;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodorder.core.response.RestResponse;
import com.thanh.foodorder.core.util.annotation.RateLimit;
import com.thanh.foodorder.core.util.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor kiểm tra hạn mức gọi API dựa trên annotation @RateLimit và Client IP.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }

        if (rateLimit == null) {
            return true;
        }

        String clientIp = getClientIp(request);
        String actionKey = method.getDeclaringClass().getSimpleName() + "#" + method.getName() + ":" + clientIp;

        boolean allowed = rateLimitService.tryConsume(
                actionKey,
                rateLimit.capacity(),
                rateLimit.refillTokens(),
                rateLimit.durationInSeconds());

        if (!allowed) {
            sendRateLimitError(response, rateLimit.durationInSeconds());
            return false;
        }

        return true;
    }

    /**
     * Lấy IP thực tế của Client (hỗ trợ qua Proxy / Load Balancer / Cloudflare).
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR",
                "X-Real-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Nếu header chứa chuỗi nhiều IP (do qua nhiều proxy), lấy IP đầu tiên
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Gửi phản hồi chuẩn HTTP 429 Too Many Requests theo format RestResponse của hệ thống.
     */
    private void sendRateLimitError(HttpServletResponse response, int retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));

        RestResponse<Object> errorResponse = new RestResponse<>();
        errorResponse.setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.setError(ErrorCode.TOO_MANY_REQUESTS.getCode());
        errorResponse.setMessage(ErrorCode.TOO_MANY_REQUESTS.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
