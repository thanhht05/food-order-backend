package com.thanh.foodorder.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.foodorder.domain.Conversation;
import com.thanh.foodorder.domain.Message;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.dto.request.SendMessageRequest;
import com.thanh.foodorder.dto.response.MessageResponse;
import com.thanh.foodorder.enums.SenderRole;
import com.thanh.foodorder.repository.ConversationRepository;
import com.thanh.foodorder.repository.MessageRepository;
import com.thanh.foodorder.util.event.MessageCreatedEvent;

@Service

public class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ConversationRepository conversationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MessageService(MessageRepository messageRepository, UserService userService,
            ConversationRepository conversationRepository, ApplicationEventPublisher eventPublisher) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.conversationRepository = conversationRepository;
        this.eventPublisher = eventPublisher;

    }

    @Transactional
    public void markMessagesAsRead(Long conversationId) {

        messageRepository.markUserMessagesAsRead(conversationId);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long conversationId) {

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long userId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(
                request.getConversationId()).orElseThrow(() -> new RuntimeException("Conversation not found"));
        User user = this.userService.getUserById(userId);

        Message message = new Message();

        message.setConversation(conversation);
        message.setSender(user);
        if (user.getRole().getName().equals("ADMIN")) {
            message.setSenderRole(SenderRole.ADMIN);
        } else {
            message.setSenderRole(SenderRole.USER);
        }
        message.setContent(request.getContent());
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        eventPublisher.publishEvent(
                new MessageCreatedEvent(saved));

        return MessageResponse.from(saved);
    }

}
