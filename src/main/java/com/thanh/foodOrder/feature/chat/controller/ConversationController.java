package com.thanh.foodorder.feature.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.chat.domain.Conversation;
import com.thanh.foodorder.feature.chat.dto.ConversationResponse;
import com.thanh.foodorder.feature.chat.service.ConversationService;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class ConversationController {
    private final ConversationService conversationService;
    private final UserService userService;

    public ConversationController(ConversationService conversationService, UserService userService) {
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @GetMapping("conversations")
    public ResponseEntity<List<ConversationResponse>> getAllConversations() {

        return ResponseEntity.ok(this.conversationService.getALl());
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> getOrCreateConversation() {

        String email = JwtUtil.getCurrentUserLogin().orElse("");
        User user = this.userService.getUserByEmail(email);

        Conversation conversation = conversationService.getOrCreateConversation(user.getId());

        return ResponseEntity.ok(
                ConversationResponse.from(conversation));
    }

    @PutMapping("conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> putMethodName(
            @PathVariable(value = "conversationId") Long conversationId) {

        Conversation conversation = conversationService.getConversationById(conversationId);

        return ResponseEntity.ok(this.conversationService.updateStatus(conversation));
    }

}
