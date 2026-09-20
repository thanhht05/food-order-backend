package com.thanh.foodorder.core.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Frontend ReactJS sẽ kết nối đến endpoint này
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*").withSockJS(); // Cho phép React gọi chéo domain (CORS)

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các channel mà server sẽ gửi message xuống client
        registry.enableSimpleBroker("/topic");

        // Prefix cho các request từ client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {

        registration.interceptors(authInterceptor);
    }
}
