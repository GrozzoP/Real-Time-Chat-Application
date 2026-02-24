package com.grosso.chat.model;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Notification {

    private String chatID;
    private String content;
    private String senderID;
    private String receiverID;
    private String chatName;
    private MessageType messageType;
    private NotificationType notificationType;
    private byte[] media;
}
