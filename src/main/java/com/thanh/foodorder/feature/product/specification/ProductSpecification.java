package com.thanh.foodorder.feature.product.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import com.thanh.foodorder.feature.product.domain.Product;
import com.thanh.foodorder.feature.product.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {
    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + keyword.toLowerCase().trim() + "%");
        };
    }

    public static Specification<Product> hasCategory(List<String> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }

            List<String> cleanCategories = categories.stream()
                    .filter(c -> c != null && !c.isBlank())
                    .flatMap(c -> Arrays.stream(c.split(",")))
                    .map(String::trim)
                    .filter(c -> !c.isEmpty())
                    .toList();

            if (cleanCategories.isEmpty()) {
                return null;
            }

            List<Predicate> predicates = new ArrayList<>();

            List<String> lowerNames = cleanCategories.stream()
                    .map(String::toLowerCase)
                    .toList();
            predicates.add(cb.lower(root.get("category").get("name")).in(lowerNames));

            List<Long> ids = new ArrayList<>();
            for (String c : cleanCategories) {
                try {
                    ids.add(Long.parseLong(c));
                } catch (NumberFormatException ignored) {
                }
            }
            if (!ids.isEmpty()) {
                predicates.add(root.get("category").get("id").in(ids));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
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

    public static Specification<Product> status(ProductStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }

            if (status == ProductStatus.ACTIVE) {
                return cb.or(
                        cb.equal(root.get("status"), ProductStatus.ACTIVE),
                        cb.isNull(root.get("status")));
            }

            return cb.equal(root.get("status"), status);
        };
    }
}
