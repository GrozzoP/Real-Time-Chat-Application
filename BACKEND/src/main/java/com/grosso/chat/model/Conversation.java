package com.grosso.chat.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

@Table(name = "conversation")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Conversation {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Lob
    @Column(name = "id_conversation")
    private String convID;

    @Column(name = "from_user")
    private Long fromUser;

    @Column(name = "to_user")
    private Long toUser;

    @Column(name = "created_at")
    @CreatedDate
    private Timestamp time;

    @Column(name = "last_modified")
    @LastModifiedDate
    private Timestamp lastModified;

    @Lob
    @Column(name = "content")
    private String content;

    @Lob
    @Column(name = "delivery_status")
    private String deliveryStatus;
}
