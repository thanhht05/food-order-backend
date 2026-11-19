package com.thanh.foodorder.feature.ai.dto;

public record GroqMessage(
        String role,
        String content) {
}