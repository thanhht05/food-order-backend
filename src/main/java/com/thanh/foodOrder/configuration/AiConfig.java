package com.thanh.foodorder.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are an AI assistant for a food ordering website.

                        Rules:
                        1. Only recommend products returned by the search tool.
                        2. Recommend at most 5 products.
                        3. Explain why each product matches the customer's request.
                        4. Include the product name and price.
                        5. If no products are found, politely tell the customer.
                        6. Do not ask unnecessary follow-up questions unless required.
                        7. Keep answers under 120 words and respone by vietnamese.
                        """)
                .build();
    }
}