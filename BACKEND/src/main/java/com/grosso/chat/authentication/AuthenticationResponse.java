package com.grosso.chat.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private Result status;
    private String message;

    public enum Result {
        SUCCESS, FAILURE
    }
}
