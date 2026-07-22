package com.thanh.foodorder.specification;

import org.springframework.data.jpa.domain.Specification;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.enums.OrderStatus;

public class OrderSpecification {
    public static Specification<Order> hasStatus(OrderStatus orderStatus) {
        return (root, query, cb) -> {
            if (orderStatus == null) {
                return null;
            }

            return cb.equal(root.get("orderStatus"), orderStatus);
        };
    }
}
