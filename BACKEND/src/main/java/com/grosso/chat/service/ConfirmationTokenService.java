package com.grosso.chat.service;

import com.grosso.chat.model.ConfirmationToken;
import com.grosso.chat.repository.ConfirmationTokenRepository;
import com.grosso.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserService userService;

    public void save(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    public ConfirmationToken getTokenByUserID(Long userID) {
        return confirmationTokenRepository.findByUser(userService.getUserById(userID))
                .orElseThrow(() -> new IllegalStateException("The confirmation token wasn't found!"));
    }

    public ConfirmationToken getConfirmationTokenByToken(String token) {
        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("The confirmation token wasn't found!"));
    }

    public boolean setConfirmedAt(String token) {
        return confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }
}
