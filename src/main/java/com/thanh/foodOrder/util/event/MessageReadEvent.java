package com.thanh.foodorder.util.event;

import lombok.Getter;

@Getter
public class MessageReadEvent {
    private final Long conversationId;

    public MessageReadEvent(Long conversationId) {
        this.conversationId = conversationId;
    }

}
