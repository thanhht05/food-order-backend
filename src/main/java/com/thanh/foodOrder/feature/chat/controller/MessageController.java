package com.thanh.foodorder.feature.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PutMapping;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.chat.dto.MessageResponse;
import com.thanh.foodorder.feature.chat.dto.SendMessageRequest;
import com.thanh.foodorder.feature.chat.service.MessageService;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

@RestController
@RequestMapping("/api/v1")

public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PutMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Void> markMessageIsRead(@PathVariable(value = "conversationId") Long conversationId) {
        // TODO: process PUT request
        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User user = this.userService.getUserByEmail(email);

        messageService.markMessagesAsRead(conversationId, user);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable(value = "conversationId") Long conversationId) {

        return ResponseEntity.ok(
                messageService.getMessages(conversationId));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {

        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User user = this.userService.getUserByEmail(email);

        MessageResponse response = messageService.sendMessage(
                user.getId(),
                request);

        return ResponseEntity.ok(response);
    }
}
