package com.grosso.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor @AllArgsConstructor
@Builder
@Getter @Setter
public class UnseenMessageCountResponse {
    private Long fromUser;
    private Long count;
}
