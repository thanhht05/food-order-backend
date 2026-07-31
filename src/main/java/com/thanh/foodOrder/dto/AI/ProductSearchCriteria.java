package com.thanh.foodorder.dto.AI;

public record ProductSearchCriteria(
        String keyword,
        String category,
        Double price) {
}