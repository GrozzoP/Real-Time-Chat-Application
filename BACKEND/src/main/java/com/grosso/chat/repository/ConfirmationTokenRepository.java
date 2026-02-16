package com.grosso.chat.repository;

import com.grosso.chat.model.ConfirmationToken;
import com.grosso.chat.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);

    Optional<ConfirmationToken> findByUser(User user);

    @Modifying
    @Query("UPDATE ConfirmationToken c SET c.confirmedAt = ?2 WHERE c.token = ?1")
    @Transactional
    Boolean updateConfirmedAt(String token, LocalDateTime confirmedAt);
}
