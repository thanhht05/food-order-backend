package com.thanh.foodorder.specification;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.thanh.foodorder.domain.Product;

public class ProductSpecification {
    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategory(List<String> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
            return root.get("category")
                    .get("name")
                    .in(categories);
        };
    }

    public static Specification<Product> priceFrom(BigDecimal from) {
        return (root, query, cb) -> {
            if (from == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("price"), from);
        };
    }

    public static Specification<Product> priceTo(BigDecimal to) {
        return (root, query, cb) -> {
            if (to == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("price"), to);
        };
    }
}
