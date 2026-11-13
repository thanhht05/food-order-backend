package com.thanh.foodorder.feature.auth.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.thanh.foodorder.feature.user.domain.User;

import lombok.extern.log4j.Log4j2;

/**
 * Quản lý đăng nhập đơn phiên (single active session):
 * mỗi tài khoản chỉ có 1 phiên hoạt động. Phiên mới thay thế phiên cũ
 * và phiên cũ được thông báo real-time qua kênh WebSocket riêng của user.
 *
 * Tách biệt với tokenVersion (chỉ dùng cho đổi mật khẩu).
 */
@Service
@Log4j2
public class LoginSessionService {

    /** Client subscribe: /user/queue/session */
    public static final String SESSION_QUEUE = "/queue/session";
    public static final String EVENT_SESSION_REPLACED = "SESSION_REPLACED";

    private final SimpMessagingTemplate messagingTemplate;

    public LoginSessionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Tạo phiên mới cho user (chỉ gán vào entity, caller chịu trách nhiệm save)
     * và gửi thông báo tới phiên cũ (nếu có).
     *
     * @return sessionId mới
     */
    public String startNewSession(User user) {
        String newSessionId = UUID.randomUUID().toString();
        boolean hadSession = user.getActiveSessionId() != null;
        user.setActiveSessionId(newSessionId);

        if (hadSession) {
            notifySessionReplaced(user.getEmail());
        }
        return newSessionId;
    }

    private void notifySessionReplaced(String email) {
        try {
            this.messagingTemplate.convertAndSendToUser(
                    email,
                    SESSION_QUEUE,
                    Map.of(
                            "type", EVENT_SESSION_REPLACED,
                            "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            // Không để lỗi WebSocket làm hỏng đăng nhập; phiên cũ vẫn bị chặn ở JwtFilter.
            log.warn("Cannot send SESSION_REPLACED to {}: {}", email, e.getMessage());
        }
    }
}

