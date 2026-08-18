package com.thanh.foodorder.configuration;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.thanh.foodorder.util.JwtUtil;

import lombok.RequiredArgsConstructor;

import java.util.Collections;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null &&
                    authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                String email = jwtUtil.extractUsername(token);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.emptyList());

                accessor.setUser(authentication);
            }
        }

        return message;
    }
}
