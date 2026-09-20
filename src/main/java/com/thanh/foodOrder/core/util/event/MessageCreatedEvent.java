package com.thanh.foodorder.core.util.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.chat.domain.Message;

@Getter
@Setter
public class MessageCreatedEvent {
    private final Message message;

    public MessageCreatedEvent(Message message) {
        this.message = message;
    }
}
