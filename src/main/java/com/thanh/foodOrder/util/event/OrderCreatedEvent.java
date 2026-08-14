package com.thanh.foodorder.util.event;

import com.thanh.foodorder.domain.Order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderCreatedEvent {
    private final Order order;
}
