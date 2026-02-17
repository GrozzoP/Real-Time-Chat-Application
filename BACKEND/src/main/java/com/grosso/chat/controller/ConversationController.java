package com.grosso.chat.controller;

import com.grosso.chat.model.ChatMessage;
import com.grosso.chat.model.UnseenMessageCountResponse;
import com.grosso.chat.model.UserConnection;
import com.grosso.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/friends")
    public List<UserConnection> getUserFriends() {
        return conversationService.getUserFriends();
    }

    @GetMapping("/unseenMessages")
    public List<UnseenMessageCountResponse> getUnseenMessageCount() {
        return conversationService.getUnseenMessagesCount();
    }

    @GetMapping("/unseenMessages/{fromUserID}")
    public List<ChatMessage> getUnseenMessages(@PathVariable("fromUserID") Long fromUserID) {
        return conversationService.getUnseenMessages(fromUserID);
    }

    @PutMapping("/setReadMessages")
    public List<ChatMessage> setReadMessages(@RequestBody List<ChatMessage> chatMessages) {
        return conversationService.setReadMessages(chatMessages);
    }
}
