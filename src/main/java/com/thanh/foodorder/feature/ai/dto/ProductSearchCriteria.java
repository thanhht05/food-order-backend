package com.thanh.foodorder.feature.ai.dto;

public record ProductSearchCriteria(
        String keyword,
        String category,
        Double price) {
}