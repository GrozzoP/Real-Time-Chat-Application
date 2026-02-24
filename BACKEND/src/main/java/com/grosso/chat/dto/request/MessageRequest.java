package com.grosso.chat.dto.request;

import com.grosso.chat.model.MessageType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageRequest {

    private String content;
    private String senderID;
    private String receiverID;
    private MessageType type;
    private String chatID;
}
