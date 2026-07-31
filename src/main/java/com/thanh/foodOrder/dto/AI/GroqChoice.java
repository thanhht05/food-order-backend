package com.thanh.foodorder.dto.AI;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqChoice(
        Integer index,
        GroqMessage message,

        @JsonProperty("finish_reason") String finishReason) {
}