package com.grosso.chat.mapper;

import com.grosso.chat.dto.response.ChatResponse;
import com.grosso.chat.model.Chat;
import org.springframework.stereotype.Service;

@Service
public class ChatMapper {

    public ChatResponse toChatResponse(Chat chat, String senderID) {
        return ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getChatName(senderID))
                .unreadCount(chat.getUnreadMessages(senderID))
                .lastMessage(chat.getLastMessage())
                .lastMessageTime(chat.getLastMessageTime())
                .isRecipientOnline(chat.getRecipient().isUserOnline())
                .senderID(chat.getSender().getId())
                .receiverID(chat.getRecipient().getId())
                .build();
    }
}
