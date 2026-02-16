package com.grosso.chat.service;

import com.grosso.chat.model.User;
import com.grosso.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "User with email %s not found!";
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username)));
    }

    public int enableUser(String email) {
        return userRepository.enableUser(email);
    }

    public User getCurrentUser() throws AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthenticationException("The credentials are bad!"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("The user wasn't found!"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("The user wasn't found!"));
    }

    public List<User> getUsersById(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void save(User user) {
        userRepository.save(user);
    }
}
