package com.thanh.foodorder.feature.chat.dto;

import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.chat.domain.Conversation;
import com.thanh.foodorder.feature.chat.enums.ConversationStatus;

@Getter
@Setter
@Builder
public class ConversationResponse {

    private Long id;

    private Long userId;

    private ConversationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // convert Entity to DTO
    public static ConversationResponse from(
            Conversation conversation) {

        return ConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .status(conversation.getStatus())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}