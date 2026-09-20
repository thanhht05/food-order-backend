package com.thanh.foodorder.feature.ai.dto;

import java.util.List;

public record GroqResponse(
        String id,
        String model,
        List<GroqChoice> choices) {
}