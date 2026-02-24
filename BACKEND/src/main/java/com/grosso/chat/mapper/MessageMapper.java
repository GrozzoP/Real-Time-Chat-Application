package com.grosso.chat.mapper;

import com.grosso.chat.dto.response.MessageResponse;
import com.grosso.chat.file.FileUtils;
import com.grosso.chat.model.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageMapper {

    public MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSenderID())
                .receiverId(message.getReceiverID())
                .state(message.getMessageState())
                .type(message.getMessageType())
                .createdAt(message.getCreatedDate())
                .media(FileUtils.readFileFromLocation(message.getMediaFilePath()))
                .build();
    }
}
