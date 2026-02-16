package com.grosso.chat.mapper;

import com.grosso.chat.model.ChatMessage;
import com.grosso.chat.model.Conversation;
import com.grosso.chat.model.MessageType;
import com.grosso.chat.model.User;
import com.grosso.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatMessageMaper {
    private final UserService userService;

    public List<ChatMessage> toChatMessages(
            List<Conversation> conversations,
            User user,
            MessageType messageType) {

        List<Long> fromUserIDs = conversations.stream().map(Conversation::getFromUser).toList();
        Map<Long, String> fromUserIdsToUsername = userService.getUsersById(fromUserIDs).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return conversations.stream()
                .map(c -> toChatMessage(c, user, fromUserIdsToUsername, messageType))
                .toList();
    }

    private static ChatMessage toChatMessage(
            Conversation e,
            User user,
            Map<Long, String> fromUserIdsToUsername,
            MessageType messageType) {
        return ChatMessage.builder()
                .id(e.getId())
                .messageType(messageType)
                .content(e.getContent())
                .receiverID(e.getToUser())
                .receiverUsername(user.getUsername())
                .senderID(e.getFromUser())
                .senderUsername(fromUserIdsToUsername.get(e.getFromUser()))
                .build();
    }
}
