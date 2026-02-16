package com.grosso.chat.service;

import com.grosso.chat.model.*;
import com.grosso.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusService {
    private final Set<Long> onlineUsers;
    private final SimpMessageSendingOperations messageTemplate;
    private final Map<Long, Set<String>> userSubscribed = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public void addOnlineUser(Principal principal) {
        if(principal != null) {
            User user = getUserFromPrincipal(principal);

            log.info("{} is online", user.getUsername());

            for(Long id : onlineUsers) {
                messageTemplate.convertAndSend("/topic/presence/" + id,
                        (Object) Map.of("type", "FRIEND_ONLINE", "userID", user.getId()));
            }

            onlineUsers.add(user.getId());
        }
    }

    public void removeOnlineUser(Principal principal) {
        if(principal != null) {
            User user = getUserFromPrincipal(principal);

            log.info("Disconnected user: {}", user.getUsername());

            onlineUsers.remove(user.getId());
            userSubscribed.remove(user.getId());

            for(Long id : onlineUsers) {
                messageTemplate.convertAndSend("/topic/presence/" + id,
                        (Object) Map.of("type", "FRIEND_OFFLINE", "userID", user.getId()));
            }
        }
    }

    public boolean isUserOnline(Long userID) {
        return onlineUsers.contains(userID);
    }

    private User getUserFromPrincipal(Principal principal) {
        if(principal instanceof UsernamePasswordAuthenticationToken auth) {
            return (User) auth.getPrincipal();
        }

        throw new IllegalStateException("The user wasn't found!");
    }

    public List<UserResponse> getOnlineUsersDetails() {
        return userRepository.findAllById(onlineUsers).stream()
                .map(u -> new UserResponse(
                        u.getId(), u.getUsername(), u.getEmail()
                )).toList();
    }

    public void addUserSubscribed(Principal principal, String subscribedChannel) {
        User user = getUserFromPrincipal(principal);
        log.info("{} subscribed to {}", user.getUsername(), subscribedChannel);

        Set<String> subscriptions = userSubscribed.getOrDefault(user.getId(), new HashSet<>());
        subscriptions.add(subscribedChannel);
        userSubscribed.put(user.getId(), subscriptions);
    }

    public void removeUserSubscribed(Principal principal, String subscribedChannel) {
        User user = getUserFromPrincipal(principal);
        log.info("{} unsubscribed {}", user.getUsername(), subscribedChannel);

        Set<String> subscriptions = userSubscribed.getOrDefault(user.getId(), new HashSet<>());
        subscriptions.remove(subscribedChannel);
        userSubscribed.put(user.getId(), subscriptions);
    }

    public boolean isUserSubscribed(Long userID, String subscription) {
        Set<String> subscriptions = userSubscribed.getOrDefault(userID, new HashSet<>());
        return subscriptions.contains(subscription);
    }

    public void notifySender(
            Long senderID,
            List<Conversation> entities,
            EMessageDeliveryStatus messageDeliveryStatus) {

        if(!isUserOnline(senderID)) {
            log.info("{} is not online, cannot inform the socket!", senderID.toString());
            return;
        }

        List<MessageDeliveryStatusUpdate> messageDeliveryStatusUpdates =
                entities.stream()
                        .map(
                                e ->
                                        MessageDeliveryStatusUpdate.builder()
                                                .id(e.getId())
                                                .messageDeliveryStatus(messageDeliveryStatus)
                                                .content(e.getContent())
                                                .build()
                        ).toList();

        for(Conversation conversation : entities) {
            messageTemplate.convertAndSend(
                    "/topic/" + senderID,
                    ChatMessage.builder()
                            .id(conversation.getId())
                            .messageDeliveryStatusUpdates(messageDeliveryStatusUpdates)
                            .messageType(MessageType.MESSAGE_DELIVERY_UPDATE)
                            .content(conversation.getContent())
                            .build()
            );
        }
    }

    public Map<String, Set<String>> getUserSubscribed() {
        Map<String, Set<String>> result = new HashMap<>();

        List<User> users = userRepository.findAllById(onlineUsers);
        users.forEach(user -> result.put(user.getUsername(), userSubscribed.get(user.getId())));

        return result;
    }
}
