package com.thanh.foodorder.dto.AI;

import java.math.BigDecimal;

public record ProductAiResponse(
                Long id,
                String name,
                BigDecimal price,
                String img) {
}