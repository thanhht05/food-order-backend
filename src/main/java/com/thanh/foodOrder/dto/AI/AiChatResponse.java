package com.thanh.foodorder.dto.AI;

import java.util.List;

public record AiChatResponse(
        String message,
        List<ProductAiResponse> products) {
}
