package com.thanh.foodOrder.configuration;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh.foodOrder.domain.respone.RestResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // TODO: blacklist token nếu dùng JWT
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // nếu dùng HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // xóa cookie

        response.addCookie(cookie);

        // tạo response chuẩn project
        RestResponse<Object> rest = new RestResponse<>();
        rest.setStatusCode(HttpStatus.OK.value());
        rest.setError(null);
        rest.setMessage(null);
        rest.setData("Logout success");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(rest));
    }
}