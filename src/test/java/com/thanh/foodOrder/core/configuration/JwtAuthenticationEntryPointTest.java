package com.thanh.foodorder.core.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JwtAuthenticationEntryPointTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(mapper);

    private JsonNode commence(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new InsufficientAuthenticationException("Full authentication required"));
        assertEquals(401, response.getStatus());
        return mapper.readTree(response.getContentAsString());
    }

    @Test
    @DisplayName("Đăng nhập ở nơi khác → error = ERR_LOGGED_IN_ELSEWHERE")
    void loggedInElsewhere() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtFilter.AUTH_ERROR_ATTR, AuthErrorCode.LOGGED_IN_ELSEWHERE);

        JsonNode body = commence(request);

        assertEquals("ERR_LOGGED_IN_ELSEWHERE", body.get("error").asText());
        assertEquals(AuthErrorCode.LOGGED_IN_ELSEWHERE.getMessage(), body.get("message").asText());
    }

    @Test
    @DisplayName("Đổi mật khẩu → error = ERR_PASSWORD_CHANGED")
    void passwordChanged() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtFilter.AUTH_ERROR_ATTR, AuthErrorCode.PASSWORD_CHANGED);

        JsonNode body = commence(request);

        assertEquals("ERR_PASSWORD_CHANGED", body.get("error").asText());
    }

    @Test
    @DisplayName("Không có mã lỗi → giữ nguyên response cũ")
    void defaultBehavior() throws Exception {
        JsonNode body = commence(new MockHttpServletRequest());

        assertEquals("Token is expired or invalid", body.get("message").asText());
        assertEquals("Full authentication required", body.get("error").asText());
    }
}

