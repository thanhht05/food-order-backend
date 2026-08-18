package com.thanh.foodorder.dto.response;

import java.time.LocalDateTime;

import com.thanh.foodorder.domain.Conversation;
import com.thanh.foodorder.enums.ConversationStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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