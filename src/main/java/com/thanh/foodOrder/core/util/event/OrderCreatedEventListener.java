package com.thanh.foodorder.core.util.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


import lombok.RequiredArgsConstructor;
import com.thanh.foodorder.feature.order.domain.Order;
import com.thanh.foodorder.feature.order.dto.AdminOrderResponseDTO;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedEvent event) {

        Order order = event.getOrder();

        AdminOrderResponseDTO response = AdminOrderResponseDTO.from(order);

        messagingTemplate.convertAndSend(
                "/topic/admin/orders",
                response);
    }
}
