package com.thanh.foodorder.dto.AI;

import java.util.List;

public record GroqRequest(
        String model,
        List<GroqMessage> messages) {
}