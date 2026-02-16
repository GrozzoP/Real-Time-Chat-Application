package com.grosso.chat.service;

import com.grosso.chat.config.UserDetailsImpl;
import com.grosso.chat.mapper.ChatMessageMaper;
import com.grosso.chat.model.*;
import com.grosso.chat.repository.ConversationRepository;
import com.grosso.chat.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final SecurityUtils securityUtils;
    private final ChatMessageMaper chatMessageMaper;
    private final UserService userService;
    private final StatusService statusService;

    public void save(Conversation conversation) {
        conversationRepository.save(conversation);
    }

    public List<Conversation> saveAll(List<Conversation> conversations) {
        return conversationRepository.saveAll(conversations);
    }

    public List<Conversation> getAllConversationsById(List<Long> ids) {
        return conversationRepository.findAllById(ids);
    }

    public List<Conversation> findUnseenMessagesCount(Long toUser) {
        return conversationRepository.findUnseenMessagesCount(toUser)
                .orElseThrow(() -> new IllegalStateException("The conversation doesn't exist!"));
    }

    public List<Conversation> findUnseenMessages(Long fromUser, Long toUser) {
        return conversationRepository.findUnseenMessages(toUser, fromUser)
                .orElseThrow(() -> new IllegalStateException("The conversation doesn't exist!"));
    }

    public List<UserConnection> getUserFriends() {
        User user = securityUtils.getUser();
        String username = user.getUsername();

        List<User> users = userService.getAllUsers();
        User thisUser = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);

        return users.stream()
                .filter(u -> !u.getUsername().equals(username))
                .map(
                        u ->
                                UserConnection.builder()
                                        .id(u.getId())
                                        .connectionUsername(u.getUsername())
                                        .convID(getConvID(u, thisUser))
                                        .unSeen(0)
                                        .isOnline(statusService.isUserOnline(u.getId()))
                                        .build()
                ).toList();
    }

    public List<ChatMessage> getUnseenMessages(Long fromUserID) {
        List<ChatMessage> result = new ArrayList<>();
        User user = securityUtils.getUser();
        List<Conversation> unseenMessages =
                this.findUnseenMessages(user.getId(), fromUserID);

        if(!(CollectionUtils.isEmpty(unseenMessages))) {
            log.info("there are some unseen messages for {} from {}", user.getUsername(), fromUserID);
            updateMessageDelivery(fromUserID, unseenMessages, EMessageDeliveryStatus.SEEN);
        }

        return result;
    }

    private void updateMessageDelivery(
            Long userID,
            List<Conversation> unseenMessages,
            EMessageDeliveryStatus messageDeliveryStatus) {
        unseenMessages.forEach(m -> m.setDeliveryStatus(messageDeliveryStatus.toString()));
        statusService.notifySender(userID, unseenMessages, messageDeliveryStatus);
        this.saveAll(unseenMessages);
    }

    public List<ChatMessage> setReadMessages(List<ChatMessage> chatMessages) {
        List<Long> messageIds = chatMessages.stream().map(ChatMessage::getId).toList();
        List<Conversation> conversations = this.getAllConversationsById(messageIds);
        conversations.forEach(message -> message.setDeliveryStatus(EMessageDeliveryStatus.SEEN.toString()));
        List<Conversation> saved = this.saveAll(conversations);

        return chatMessageMaper.toChatMessages(saved, securityUtils.getUser(), MessageType.CHAT);
    }

    private String getConvID(User user1, User user2) {
        String id1 = user1.getId().toString();
        String id2 = user2.getId().toString();

        return id1.compareTo(id2) > 0 ? (id2 + "_" + id1) : (id1 + "_" + id2);
    }
}
