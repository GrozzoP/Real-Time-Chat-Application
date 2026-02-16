package com.grosso.chat.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    @Column(unique = true)
    private String token;

    private TokenType tokenType = TokenType.BEARER;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
