package com.thanh.foodorder.feature.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thanh.foodorder.feature.chat.domain.Conversation;
import com.thanh.foodorder.feature.chat.enums.ConversationStatus;


@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUserIdAndStatus(
            Long userId,
            ConversationStatus status);

    List<Conversation> findAllByOrderByUpdatedAtDesc();

    Optional<Conversation> findById(Long id);

}
