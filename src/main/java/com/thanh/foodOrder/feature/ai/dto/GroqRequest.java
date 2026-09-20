package com.thanh.foodorder.feature.ai.dto;

import java.util.List;

public record GroqRequest(
        String model,
        List<GroqMessage> messages) {
}