package com.thanh.foodorder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thanh.foodorder.domain.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(
            Long conversationId);

    @Modifying
    @Query("""
                UPDATE Message m
                SET m.isRead = true
                WHERE m.conversation.id = :conversationId
                  AND m.senderRole = 'USER'
            """)
    void markUserMessagesAsRead(
            @Param("conversationId") Long conversationId);
}
