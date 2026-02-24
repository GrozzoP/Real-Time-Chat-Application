package com.grosso.chat.model;

import com.grosso.chat.constants.MessageConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "message")
@NamedQuery(name = MessageConstants.FIND_MESSAGES_BY_CHAT_ID,
        query = "SELECT m FROM Message m WHERE m.chat.id =: chatID ORDER BY m.createdDate"
)
@NamedQuery(name = MessageConstants.FIND_MESSAGES_TO_SEEN_BY_CHAT,
        query = "UPDATE Message SET state =: newState WHERE chat.id =: chatID"
)

public class Message extends BaseAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    private MessageState messageState;

    @ManyToOne
    @JoinColumn(name = "id_chat")
    private Chat chat;

    @Column(name = "id_sender", nullable = false)
    private String senderID;

    @Column(name = "id_receiver", nullable = false)
    private String receiverID;

    private String mediaFilePath;
}
