package com.thanh.foodorder.util.event;

import com.thanh.foodorder.domain.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageCreatedEvent {
    private final Message message;

    public MessageCreatedEvent(Message message) {
        this.message = message;
    }
}
