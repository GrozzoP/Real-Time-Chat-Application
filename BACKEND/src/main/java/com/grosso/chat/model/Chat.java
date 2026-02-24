package com.grosso.chat.model;

import com.grosso.chat.constants.ChatConstants;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "chat")
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID,
            query = "SELECT DISTINCT c FROM Chat c WHERE c.id_sender = :senderID OR c.recipient.id =: senderID ORDER BY createdDate DESC")
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER,
            query = "SELECT DISTINCT c FROM Chat c WHERE (c.sender.id =: senderID AND c.recipient.id =: recipientID OR (c.sender.id = :recipientId AND c.recipient.id = :senderId) ORDER BY createdDate DESC")
public class Chat extends BaseAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_sender")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "id_recipient")
    private User recipient;

    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
    private List<Message> messages;

    @Transient
    public String getChatName(String senderID) {
        if(recipient.getId().equals(senderID))
            return sender.getFirstName() + " " + sender.getLastName();
        else
            return recipient.getFirstName() + " " + recipient.getLastName();
    }

    @Transient
    public long getUnreadMessages(String senderID) {
        return this.messages
                .stream()
                .filter(m -> m.getReceiverID().equals(senderID))
                .filter(m -> MessageState.SENT == m.getMessageState())
                .count();
    }

    @Transient
    public String getLastMessage() {
        if(messages != null && !messages.isEmpty()) {
            if(messages.getFirst().getMessageType() != MessageType.TEXT)
                return "Attachment";
            else
                return messages.getFirst().getContent();
        }

        return null;
    }

    @Transient
    public LocalDateTime getLastMessageTime() {
        if(messages != null && !messages.isEmpty()) {
            return messages.getFirst().getCreatedDate();
        }

        return null;
    }
}
