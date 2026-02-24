package com.grosso.chat.repository;

import com.grosso.chat.constants.MessageConstants;
import com.grosso.chat.model.Message;
import com.grosso.chat.model.MessageState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(name = MessageConstants.FIND_MESSAGES_BY_CHAT_ID)
    List<Message> findMessagesByChatId(@Param("chatID") String chatID);

    @Query(name = MessageConstants.FIND_MESSAGES_TO_SEEN_BY_CHAT)
    void setMessagesToSeenByChatId(@Param("chatID") String chatID, @Param("newState") MessageState state);
}
