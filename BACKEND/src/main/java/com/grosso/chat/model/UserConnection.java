package com.grosso.chat.model;

import lombok.*;

@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter @Setter
public class UserConnection {
    private Long id;
    private String connectionUsername;
    private String convID;
    private int unSeen;
    private boolean isOnline;
}
