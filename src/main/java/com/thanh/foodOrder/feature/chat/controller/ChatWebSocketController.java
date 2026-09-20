package com.thanh.foodorder.feature.chat.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;


import lombok.AllArgsConstructor;
import com.thanh.foodorder.feature.chat.dto.SendMessageRequest;
import com.thanh.foodorder.feature.chat.service.MessageService;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

@Controller

public class ChatWebSocketController {

    private final MessageService messageService;
    private UserService userService;

    public ChatWebSocketController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable("conversationId") Long conversationId,
            SendMessageRequest request, Principal principal) {

        String email = principal.getName();
        User user = this.userService.getUserByEmail(email);

        request.setConversationId(conversationId);

        messageService.sendMessage(user.getId(), request);
    }
}