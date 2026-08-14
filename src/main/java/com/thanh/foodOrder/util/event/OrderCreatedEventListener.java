package com.thanh.foodorder.util.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.dto.response.order.AdminOrderResponseDTO;

import lombok.RequiredArgsConstructor;

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
