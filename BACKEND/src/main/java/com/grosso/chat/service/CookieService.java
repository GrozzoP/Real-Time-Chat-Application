package com.grosso.chat.service;

import com.grosso.chat.config.SecurityCipher;
import com.grosso.chat.model.Token;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    public void addTokenCookie(String name, HttpHeaders httpHeaders, Token token) {
        Duration d = Duration.between(token.getCreatedAt(), token.getExpiresAt());
        Long duration = d.toSeconds();

        httpHeaders.add(HttpHeaders.SET_COOKIE,
                (createTokenCookie(name, token.getToken(), duration).toString()));
    }

    private HttpCookie createTokenCookie(String name, String token, Long duration) {
        String encryptedToken = SecurityCipher.encrypt(token);

        return ResponseCookie.from(name, encryptedToken)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .secure(true)
                .build();
    }

    public void addHttpOnlyCookie(String name, String value, int maxAge, HttpServletResponse response){
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    public void deleteCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name,null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    public void deleteTokenCookie(String name, HttpHeaders httpHeaders) {
        httpHeaders.add(HttpHeaders.SET_COOKIE,
                ResponseCookie.from(name, "")
                        .maxAge(0)
                        .httpOnly(true)
                        .path("/")
                        .secure(true)
                        .build()
                        .toString());
    }
}
