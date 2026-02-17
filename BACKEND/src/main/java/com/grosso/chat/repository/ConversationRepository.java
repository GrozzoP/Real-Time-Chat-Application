package com.grosso.chat.repository;

import com.grosso.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query(
            "SELECT c FROM Conversation c " +
                    "WHERE c.toUser = :toUser " +
                    "AND c.fromUser = :fromUser " +
                    "AND c.deliveryStatus IN ('NOT_DELIVERED', 'DELIVERED') " +
                    "ORDER BY c.time ASC"
    )
    Optional<List<Conversation>> findUnseenMessages(
            @Param("toUser") Long toUser,
            @Param("fromUser") Long fromUser
    );

    @Query(
            value =
                    "SELECT c FROM Conversation c WHERE c.toUser = :toUser AND c.deliveryStatus IN ('NOT_DELIVERED', 'DELIVERED')"
    )
    Optional<List<Conversation>> findUnseenMessagesCount(@Param("toUser") Long toUser);
}
