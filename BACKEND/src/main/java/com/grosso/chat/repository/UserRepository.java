package com.grosso.chat.repository;

import com.grosso.chat.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.enabled = TRUE WHERE email = ?1")
    int enableUser(String email);
}
