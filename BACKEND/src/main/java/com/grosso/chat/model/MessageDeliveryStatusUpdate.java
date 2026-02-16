package com.grosso.chat.model;

import lombok.*;

@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter @Setter
public class MessageDeliveryStatusUpdate {
    private Long id;
    private String content;
    private EMessageDeliveryStatus messageDeliveryStatus;
}
