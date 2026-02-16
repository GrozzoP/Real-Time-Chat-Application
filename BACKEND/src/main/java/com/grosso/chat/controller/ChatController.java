package com.grosso.chat.controller;

import com.grosso.chat.model.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.awt.*;

@Controller
public class ChatController {

    @MessageMapping("/chat/sendMessage/{convID}")
    public ChatMessage sendMessage(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            @DestinationVariable("convID") String conversationID
    ) {
        return chatMessage;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderUsername());
        return chatMessage;
    }
}
