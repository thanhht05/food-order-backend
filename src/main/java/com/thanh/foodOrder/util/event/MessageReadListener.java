package com.thanh.foodorder.util.event;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageReadListener {

    private final SimpMessagingTemplate messagingTemplate;

    public MessageReadListener(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingTemplate = simpMessagingTemplate;
    }

    @EventListener
    public void handleMessageRead(MessageReadEvent event) {

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + event.getConversationId() + "/read",
                event);
    }
}