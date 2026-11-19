package com.thanh.foodorder.feature.ai.dto;

import java.util.List;

public record AiChatResponse(
        String message,
        List<ProductAiResponse> products) {
}
