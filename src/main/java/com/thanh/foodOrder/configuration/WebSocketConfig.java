package com.thanh.foodOrder.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Frontend ReactJS sẽ kết nối đến endpoint này
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép React gọi chéo domain (CORS)
                .withSockJS(); // Fallback nếu browser không hỗ trợ chuẩn WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các channel mà server sẽ gửi message xuống client
        registry.enableSimpleBroker("/topic");

        // Prefix cho các request từ client gửi lên server (nếu có dùng)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
