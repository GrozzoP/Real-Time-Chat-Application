package com.grosso.chat.service;

import com.grosso.chat.dto.response.ChatResponse;
import com.grosso.chat.mapper.ChatMapper;
import com.grosso.chat.model.Chat;
import com.grosso.chat.model.User;
import com.grosso.chat.repository.ChatRepository;
import com.grosso.chat.repository.UserRepository;
import com.grosso.chat.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper mapper;
    private final SecurityUtils securityUtils;

    public List<ChatResponse> getChatsByReceiverID() {
        User user = securityUtils.getUser();
        String userID = user.getId();

        return chatRepository.findChatsBySenderID(userID)
                .stream()
                .map(c -> mapper.toChatResponse(c, userID))
                .toList();
    }

    public String createChat(String senderID, String receiverID) {
        Optional<Chat> existingChat = chatRepository.findChatsBySenderAndReceiver(senderID, receiverID);

        if(existingChat.isPresent())
            return existingChat.get().getId();

        User sender = userRepository.findById(senderID)
                .orElseThrow(() -> new EntityNotFoundException("User with ID #" + senderID + " not found!"));

        User receiver = userRepository.findById(receiverID)
                .orElseThrow(() -> new EntityNotFoundException("User with ID #" + receiverID + " not found!"));

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRecipient(receiver);

        Chat savedChat = chatRepository.save(chat);

        return savedChat.getId();
    }

}
