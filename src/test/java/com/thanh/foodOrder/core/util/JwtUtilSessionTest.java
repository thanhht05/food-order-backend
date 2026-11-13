package com.thanh.foodorder.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.thanh.foodorder.feature.auth.dto.ResponseLoginDTO;
import com.thanh.foodorder.feature.user.domain.User;

class JwtUtilSessionTest {

    private JwtUtil jwtUtil;
    private User user;
    private ResponseLoginDTO res;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.SECRET_KEY = "test-secret-key-test-secret-key-0123456789";

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFullName("User");
        user.setTokenVersion(0);

        res = new ResponseLoginDTO();
        ResponseLoginDTO.UserLogin userLogin = new ResponseLoginDTO.UserLogin();
        userLogin.setId(1L);
        userLogin.setEmail("user@example.com");
        userLogin.setFullname("User");
        res.setUserLogin(userLogin);
    }

    @Test
    @DisplayName("Access token và refresh token đều chứa claim sessionId")
    void tokensContainSessionId() {
        user.setActiveSessionId("session-A");

        assertEquals("session-A", jwtUtil.extractSessionId(jwtUtil.generateToken(user, res)));
        assertEquals("session-A", jwtUtil.extractSessionId(jwtUtil.generateRefreshToken(user, res)));
    }

    @Test
    @DisplayName("Token của phiên hiện tại hợp lệ")
    void sameSession_isActive() {
        user.setActiveSessionId("session-A");
        String token = jwtUtil.generateToken(user, res);

        assertTrue(jwtUtil.isSessionActive(token, "session-A"));
    }

    @Test
    @DisplayName("Token của phiên cũ bị vô hiệu khi đã đăng nhập ở nơi khác")
    void replacedSession_isNotActive() {
        user.setActiveSessionId("session-A");
        String oldToken = jwtUtil.generateToken(user, res);

        assertFalse(jwtUtil.isSessionActive(oldToken, "session-B"));
    }

    @Test
    @DisplayName("DB chưa có activeSessionId (user cũ) → bỏ qua kiểm tra")
    void nullSessionInDb_isActive() {
        String legacyToken = jwtUtil.generateToken(user, res); // sessionId = null

        assertTrue(jwtUtil.isSessionActive(legacyToken, null));
    }

    @Test
    @DisplayName("Token cũ không có sessionId nhưng DB đã có phiên mới → vô hiệu")
    void legacyTokenAfterNewLogin_isNotActive() {
        String legacyToken = jwtUtil.generateToken(user, res); // sessionId = null

        assertFalse(jwtUtil.isSessionActive(legacyToken, "session-B"));
    }
}

