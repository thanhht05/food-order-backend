package com.thanh.foodorder.feature.chat.dto;

import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.chat.domain.Message;
import com.thanh.foodorder.feature.chat.enums.SenderRole;

@Getter
@Setter
@Builder
public class MessageResponse {

        private Long id;

        private Long conversationId;

        private Long senderId;

        private SenderRole senderRole;

        private String content;

        private Boolean isRead;

        private LocalDateTime createdAt;

        public static MessageResponse from(Message message) {

                return MessageResponse.builder()
                                .id(message.getId())
                                .conversationId(
                                                message.getConversation().getId())
                                .senderId(
                                                message.getSender().getId())
                                .senderRole(message.getSenderRole())
                                .content(message.getContent())
                                .isRead(message.getIsRead())
                                .createdAt(message.getCreatedAt())
                                .build();
        }
}