package com.thanh.foodOrder.specification;

import org.springframework.data.jpa.domain.Specification;

import com.thanh.foodOrder.domain.Order;
import com.thanh.foodOrder.enums.OrderStatus;

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
