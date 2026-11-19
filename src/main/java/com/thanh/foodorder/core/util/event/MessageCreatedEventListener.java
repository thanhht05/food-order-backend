package com.thanh.foodorder.core.util.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


import lombok.RequiredArgsConstructor;
import com.thanh.foodorder.feature.chat.domain.Message;
import com.thanh.foodorder.feature.chat.dto.MessageResponse;

@Component
@RequiredArgsConstructor
public class MessageCreatedEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MessageCreatedEvent event) {

        Message message = event.getMessage();

        MessageResponse response = MessageResponse.from(message);

        messagingTemplate.convertAndSend(
                "/topic/chat/" +
                        message.getConversation().getId(),
                response);
    }
}
