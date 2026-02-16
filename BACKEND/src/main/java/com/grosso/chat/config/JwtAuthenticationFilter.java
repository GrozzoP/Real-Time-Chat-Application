package com.grosso.chat.config;

import com.grosso.chat.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String jwt = getJWToken(request, true);
        final String userEmail;

        if(jwt == null) {
            filterChain.doFilter(request, response);
            return;
        } else {
            userEmail = jwtService.extractUsername(jwt);

            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if(jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        }


        filterChain.doFilter(request, response);
    }

    private String getJWToken(HttpServletRequest request, boolean fromCookie) {
        if(fromCookie)
            return getJWTFromCookie(request);

        return getJWTFromRequest(request);
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7);
            return SecurityCipher.decrypt(accessToken);
        }

        return null;
    }

    private String getJWTFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(cookies == null)
            return null;

        for(Cookie cookie : cookies) {
            if((cookie.getName()).equals("ACCESS-TOKEN")) {
                String accessToken = cookie.getValue();

                return SecurityCipher.decrypt(accessToken);
            }
        }

        return null;
    }
}
