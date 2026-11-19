package com.thanh.foodorder.feature.ai.dto;

import java.math.BigDecimal;

public record ProductAiResponse(
                Long id,
                String name,
                BigDecimal price,
                String img) {
}