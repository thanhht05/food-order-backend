package com.thanh.foodorder.feature.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.thanh.foodorder.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class LoginSessionServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private LoginSessionService loginSessionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@example.com");
    }

    @Test
    @DisplayName("Lần đăng nhập đầu tiên: tạo sessionId, không gửi thông báo")
    void firstLogin_createsSession_noNotification() {
        String sessionId = loginSessionService.startNewSession(user);

        assertNotNull(sessionId);
        assertEquals(sessionId, user.getActiveSessionId());
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Đăng nhập khi đã có phiên: tạo sessionId mới và báo SESSION_REPLACED tới kênh riêng")
    @SuppressWarnings("unchecked")
    void loginWithExistingSession_replacesAndNotifies() {
        user.setActiveSessionId("old-session");

        String sessionId = loginSessionService.startNewSession(user);

        assertNotEquals("old-session", sessionId);
        assertEquals(sessionId, user.getActiveSessionId());

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq("user@example.com"), eq(LoginSessionService.SESSION_QUEUE), payload.capture());
        Map<String, Object> body = (Map<String, Object>) payload.getValue();
        assertEquals(LoginSessionService.EVENT_SESSION_REPLACED, body.get("type"));
    }

    @Test
    @DisplayName("Lỗi WebSocket không làm hỏng đăng nhập")
    void websocketFailure_doesNotThrow() {
        user.setActiveSessionId("old-session");
        doThrow(new MessagingException("broker down"))
                .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> loginSessionService.startNewSession(user));
        assertNotEquals("old-session", user.getActiveSessionId());
    }
}

