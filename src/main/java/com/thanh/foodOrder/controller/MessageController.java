package com.thanh.foodorder.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.dto.request.SendMessageRequest;
import com.thanh.foodorder.dto.response.MessageResponse;
import com.thanh.foodorder.service.MessageService;
import com.thanh.foodorder.service.UserService;
import com.thanh.foodorder.util.JwtUtil;
import org.springframework.web.bind.annotation.PutMapping;

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
    public ResponseEntity<Void> updateMessageStatus(@PathVariable(value = "conversationId") Long conversationId) {
        // TODO: process PUT request

        messageService.markMessagesAsRead(conversationId);
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
