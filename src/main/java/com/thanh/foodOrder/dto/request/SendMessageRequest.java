package com.thanh.foodorder.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    private Long conversationId; // mark this message belong what conversagtion

    private String content;
}
