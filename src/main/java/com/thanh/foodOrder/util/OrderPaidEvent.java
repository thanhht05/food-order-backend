package com.thanh.foodorder.util;

import com.thanh.foodorder.domain.Order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderPaidEvent {
    private final Order order;

}
