package com.thanh.foodorder.feature.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqChoice(
        Integer index,
        GroqMessage message,

        @JsonProperty("finish_reason") String finishReason) {
}