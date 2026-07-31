package com.thanh.foodorder.dto.AI;

import java.util.List;

public record GroqResponse(
        String id,
        String model,
        List<GroqChoice> choices) {
}