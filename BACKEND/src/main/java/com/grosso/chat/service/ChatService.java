package com.grosso.chat.service;

import com.grosso.chat.config.UserDetailsImpl;
import com.grosso.chat.model.ChatMessage;
import com.grosso.chat.model.Conversation;
import com.grosso.chat.model.EMessageDeliveryStatus;
import com.grosso.chat.model.User;
import com.grosso.chat.repository.ConversationRepository;
import com.grosso.chat.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final StatusService statusService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final ConversationService conversationService;
    private final SecurityUtils securityUtils;

    public void sendMessageToConversationID(
            ChatMessage chatMessage,
            String convID,
            SimpMessageHeaderAccessor headerAccessor) {
        User user = securityUtils.getUser();
        Long fromUserID = user.getId();
        Long toUserID = chatMessage.getReceiverID();

        populateContext(chatMessage, user);


        boolean isTargetOnline = statusService.isUserOnline(toUserID);
        boolean isTargetSubscribed = statusService.isUserSubscribed(toUserID,"/topic/" + convID);
        chatMessage.setId((new Random()).nextLong());

        Conversation.ConversationBuilder conversationBuilder = Conversation.builder();

        conversationBuilder
                .id(chatMessage.getId())
                .fromUser(fromUserID)
                .toUser(toUserID)
                .content(chatMessage.getContent())
                .convID(convID);

        if(!isTargetOnline) {
            log.info("{} is not online. Content saved in unseen messages", chatMessage.getReceiverUsername());
            conversationBuilder.deliveryStatus(EMessageDeliveryStatus.NOT_DELIVERED.toString());
            chatMessage.setMessageDeliveryStatus(EMessageDeliveryStatus.NOT_DELIVERED);
        } else if(!isTargetSubscribed) {
            log.info("{} is online but not subscribed, sending to their private subscription!", chatMessage.getReceiverUsername());
            conversationBuilder.deliveryStatus(EMessageDeliveryStatus.DELIVERED.toString());
            chatMessage.setMessageDeliveryStatus(EMessageDeliveryStatus.DELIVERED);
            simpMessageSendingOperations.convertAndSend("/topic/" + toUserID.toString(), chatMessage);
        } else {
            conversationBuilder.deliveryStatus(EMessageDeliveryStatus.SEEN.toString());
            chatMessage.setMessageDeliveryStatus(EMessageDeliveryStatus.SEEN);
        }

        conversationService.save(conversationBuilder.build());
        simpMessageSendingOperations.convertAndSend("/topic/" + convID, chatMessage);
    }

    private void populateContext(ChatMessage chatMessage, User user) {
        chatMessage.setSenderID(user.getId());
        chatMessage.setSenderUsername(user.getUsername());
    }
}
