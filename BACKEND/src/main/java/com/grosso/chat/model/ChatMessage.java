package com.grosso.chat.model;

import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private Long id;
    private String content;
    private MessageType messageType;
    private String senderUsername;
    private Long senderID;
    private String receiverUsername;
    private Long receiverID;
    private EMessageDeliveryStatus messageDeliveryStatus;
    private List<MessageDeliveryStatusUpdate> messageDeliveryStatusUpdates;
}
