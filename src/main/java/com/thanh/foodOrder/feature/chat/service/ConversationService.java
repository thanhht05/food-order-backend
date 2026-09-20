package com.thanh.foodorder.feature.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;


import jakarta.transaction.Transactional;
import com.thanh.foodorder.feature.chat.domain.Conversation;
import com.thanh.foodorder.feature.chat.dto.ConversationResponse;
import com.thanh.foodorder.feature.chat.enums.ConversationStatus;
import com.thanh.foodorder.feature.chat.repository.ConversationRepository;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserService userService;

    public ConversationService(ConversationRepository conversationRepository,
            UserService userService) {
        this.conversationRepository = conversationRepository;
        this.userService = userService;
    }

    public List<ConversationResponse> getALl() {
        return conversationRepository
                .findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @Transactional
    public Conversation getOrCreateConversation(Long userId) {

        return conversationRepository
                .findByUserIdAndStatus(
                        userId,
                        ConversationStatus.OPEN)
                .orElseGet(() -> createConversation(userId));
    }

    public Conversation getConversationById(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }

    private Conversation createConversation(Long userId) {

        User user = userService.getUserById(userId);

        Conversation conversation = new Conversation();

        conversation.setUser(user);
        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(conversation);
    }

    public ConversationResponse updateStatus(Conversation conversation) {

        conversation.setStatus(ConversationStatus.CLOSED);

        Conversation update = conversationRepository.save(conversation);

        ConversationResponse res = ConversationResponse.from(update);
        return res;
    }

}
