package com.grosso.chat.security;

import com.grosso.chat.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class SecurityUtils {

    public User getUserDetails(Principal principal) {
        if(principal instanceof UsernamePasswordAuthenticationToken auth) {
            return (User) auth.getPrincipal();
        } else
            return null;
    }

    public User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null && auth.getPrincipal() instanceof User user)
            return user;
        else
            return null;
    }
}
