package com.thanh.foodorder.dto.response;

import java.time.LocalDateTime;

import com.thanh.foodorder.domain.Message;
import com.thanh.foodorder.enums.SenderRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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