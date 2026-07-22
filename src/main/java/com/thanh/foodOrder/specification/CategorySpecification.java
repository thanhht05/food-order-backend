package com.thanh.foodorder.specification;

import org.springframework.data.jpa.domain.Specification;

import com.thanh.foodorder.domain.Category;

public class CategorySpecification {
    public static Specification<Category> all() {
        return Specification.allOf();
    }

    public static Specification<Category> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }
}
