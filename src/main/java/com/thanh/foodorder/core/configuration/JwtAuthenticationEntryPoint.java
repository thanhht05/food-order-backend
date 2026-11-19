package com.thanh.foodorder.core.configuration;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.thanh.foodorder.core.response.RestResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;

    public JwtAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        Object authError = request.getAttribute(JwtFilter.AUTH_ERROR_ATTR);
        if (authError instanceof AuthErrorCode code) {
            res.setError(code.getCode());
            res.setMessage(code.getMessage());
            mapper.writeValue(response.getWriter(), res);
            return;
        }

        res.setMessage("Token is expired or invalid");
        String errorMessage = Optional.ofNullable(authException.getCause())
                .map(Throwable::getMessage)
                .orElse(authException.getMessage());
        res.setError(errorMessage);
        mapper.writeValue(response.getWriter(), res);

    }

}
