package com.grosso.chat.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AuthenticationRequest {
    @NotEmpty(message = "Email shouldn't be empty!")
    @Email(message = "Invalid mail format!")
    private String email;

    @NotEmpty(message = "Password shouldn't be empty!")
    private String password;
}
