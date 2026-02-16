package com.grosso.chat.controller;

import com.grosso.chat.authentication.AuthenticationRequest;
import com.grosso.chat.authentication.AuthenticationResponse;
import com.grosso.chat.authentication.RegistrationRequest;
import com.grosso.chat.config.SecurityCipher;
import com.grosso.chat.model.ConfirmationToken;
import com.grosso.chat.service.AuthenticationService;
import com.grosso.chat.service.ConfirmationTokenService;
import com.grosso.chat.service.JWTService;
import com.grosso.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@CrossOrigin(
        allowCredentials = "true",
        origins = "http://localhost:4200"
)
@RequiredArgsConstructor
@RequestMapping(path = "api/auth")
public class AuthenticationController {
    private final AuthenticationService authService;
    private final ConfirmationTokenService confirmationTokenService;
    private final JWTService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @CrossOrigin
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegistrationRequest request) throws Exception {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request,
            @CookieValue(name = "ACCESS-TOKEN", required = false) String accessToken,
            @CookieValue(name = "REFRESH-TOKEN", required = false) String refreshToken
    ) throws AuthenticationException {

        try {
            UserDetails userDetails = userService.loadUserByUsername(request.getEmail());

            if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                throw new javax.naming.AuthenticationException("Bad credentials");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String decryptedAccessToken = (accessToken == null || accessToken.isBlank()) ? null : SecurityCipher.decrypt(accessToken);
            String decryptedRefreshToken = (refreshToken == null || refreshToken.isBlank()) ? null : SecurityCipher.decrypt(refreshToken);

            return authService.processAuthentication(request, decryptedAccessToken, decryptedRefreshToken);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            AuthenticationResponse body = new AuthenticationResponse(
                    AuthenticationResponse.Result.FAILURE,
                    "User not found"
            );
            return ResponseEntity.status(404).body(body);
        } catch (Exception ex) {
            AuthenticationResponse body = new AuthenticationResponse(
                    AuthenticationResponse.Result.FAILURE,
                    "Authentication failed: " + ex.getMessage()
            );
            return ResponseEntity.status(500).body(body);
        }
    }

    @GetMapping(path = "/confirm")
    public String confirm(@RequestParam("token") String token) {
        return authService.confirmToken(token) ? "Confirmed" : "Not confirmed!";
    }

    @GetMapping(path = "confirmationToken")
    public ResponseEntity<String> confirmationToken(@RequestParam("userID") Long userID) {
        ConfirmationToken confirmationToken = confirmationTokenService.getTokenByUserID(userID);

        if(confirmationToken != null)
            return ResponseEntity.ok(confirmationToken.getToken());
        else
            return ResponseEntity.notFound().build();
    }

    public ResponseEntity<AuthenticationResponse> refreshToken(
            @CookieValue(name = "REFRESH-TOKEN", required = false) String encryptedRefreshToken
    ) {
        if(encryptedRefreshToken == null || encryptedRefreshToken.isBlank()) {
            return ResponseEntity.status(401).body(new AuthenticationResponse(
                    AuthenticationResponse.Result.FAILURE, "No refresh token provided"
            ));
        }

        String decryptedRefreshToken = SecurityCipher.decrypt(encryptedRefreshToken);

        return authService.refreshToken(decryptedRefreshToken);
    }

    @PostMapping(path = "logout")
    public ResponseEntity<AuthenticationResponse> logout() {
        return authService.logout();
    }

    public ResponseEntity<AuthenticationResponse> refreshToken(
            @CookieValue(name = "ACCESS-TOKEN") String accessToken,
            @CookieValue(name = "REFRESH-TOKEN", required = false) String refreshToken
    ) {
        String decryptedAccessToken = SecurityCipher.decrypt(accessToken);
        String decryptedRefreshToken = SecurityCipher.decrypt(refreshToken);

        if(jwtService.isTokenValid(decryptedAccessToken))
            return authService.refreshToken(decryptedRefreshToken);
        else
        {
            AuthenticationResponse response =  new AuthenticationResponse(
                    AuthenticationResponse.Result.FAILURE,
                    "Auth has failed!"
            );
            return ResponseEntity.ok(response);
        }

    }
}
