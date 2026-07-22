package com.thanh.foodorder.specification;

import org.springframework.data.jpa.domain.Specification;

import com.thanh.foodorder.domain.User;

public class UserSpecification {
    // get all user
    public static Specification<User> all() {
        return Specification.allOf();
    }

    public static Specification<User> hasName(String fullName) {
        return (root, query, cb) -> {
            if (fullName == null || fullName.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
        };
    }
}
