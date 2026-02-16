package com.grosso.chat.service;

import com.grosso.chat.model.Token;
import com.grosso.chat.model.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${security.jwt.secretKey}")
    private String SECRET_KEY;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-token.expiration}")
    private long jwtRefreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Token generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration, TokenType.ACCESS);
    }

    public Token generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public Token generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtRefreshExpiration, TokenType.REFRESH);
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isRefreshToken(String token) {
        String type = extractClaim(token, claims -> claims.get("token_type", String.class));
        return TokenType.REFRESH.name().equals(type);
    }

    public boolean isTokenValid(String token) {
        boolean validation = false;
        try {
            Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            validation = true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired!");
        } catch(Exception e){
            System.out.println("Some other exception in JWT parsing!");
        }
        return validation;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Token buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            Long jwtExpiration,
            TokenType tokenType) {
        Date now = new Date();
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);

        extraClaims.put("token_type", tokenType.name());

        String token = Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();

        return new Token(
                token,
                tokenType,
                LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault())
        );
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] bytes = Base64.getDecoder().decode(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
