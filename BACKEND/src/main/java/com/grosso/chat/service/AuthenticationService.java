package com.grosso.chat.service;

import com.grosso.chat.authentication.AuthenticationRequest;
import com.grosso.chat.authentication.AuthenticationResponse;
import com.grosso.chat.authentication.RegistrationRequest;
import com.grosso.chat.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final EmailService emailService;
    private final RoleService roleService;
    private final ConfirmationTokenService confirmationTokenService;
    private final JWTService jwtService;
    private final CookieService cookieService;
    private final PasswordEncoder passwordEncoder;

    private final static String LINK_CONFIRMATION_TOKEN = "http://localhost:8070/api/auth/confirm?token=";
    private final static String JWT_COOKIE_ACCESS = "ACCESS-TOKEN";
    private final static String JWT_COOKIE_REFRESH = "REFRESH-TOKEN";


    public AuthenticationResponse register(RegistrationRequest request) {
        if(!(emailService.validateEmail(request.getEmail()))) {
            return new AuthenticationResponse(
                    AuthenticationResponse.Result.FAILURE,
                    "The email isn't valid!"
            );
        }

        if(!userService.existsByEmail(request.getEmail())) {
            return new AuthenticationResponse(
                    AuthenticationResponse.Result.FAILURE,
                    "Email is already in use!"
            );
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        String roleName = request.getRole();
        Role role = new Role();

        if(roleName == null) {
            role = roleService.getRoleByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role wasn't found!"));
        } else {
            switch (roleName) {
                case "admin":
                    role = roleService.getRoleByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Role wasn't found!"));
            }
        }

        User user = new User(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                encryptedPassword,
                request.getEmail(),
                role
        );

        userService.save(user);

        String token = UUID.randomUUID().toString();

        User persistedUser = userService.getUserByEmail(user.getEmail());

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                persistedUser
        );

        confirmationTokenService.save(confirmationToken);

        String link = LINK_CONFIRMATION_TOKEN + token;

        emailService.send(request.getEmail(), buildEmail(request.getFirstName(), link));

        return new AuthenticationResponse(
                AuthenticationResponse.Result.SUCCESS,
                "Auth succesful. Tokens are created in cookie."
        );
    }

    @Transactional
    public boolean confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getConfirmationTokenByToken(token);

        if(confirmationToken.getConfirmedAt() != null)
            throw new IllegalStateException("Token was confirmed!");

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if(expiredAt.isBefore(LocalDateTime.now()))
            throw new IllegalStateException("Token has expired! Please ask for another token!");

        if(confirmationTokenService.setConfirmedAt(token)) {
            userService.enableUser(confirmationToken.getUser().getEmail());
            return true;
        } else
            return false;
    }

    public ResponseEntity<AuthenticationResponse> processAuthentication(
            AuthenticationRequest request,
            String accessToken,
            String refreshToken) {

        boolean isTokenValid = false, isRefreshTokenValid = false;
        User user = userService.getUserByEmail(request.getEmail());

        if(accessToken != null) {
            isTokenValid = jwtService.isTokenValid(accessToken);
        }

        if(refreshToken != null) {
            isRefreshTokenValid = jwtService.isTokenValid(refreshToken);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        Token newAccessToken, newRefreshToken;

        if(!isTokenValid && !isRefreshTokenValid) {
            newAccessToken = jwtService.generateToken(user);
            newRefreshToken = jwtService.generateRefreshToken(user);

            cookieService.addTokenCookie(JWT_COOKIE_ACCESS, responseHeaders, newAccessToken);
            cookieService.addTokenCookie(JWT_COOKIE_REFRESH, responseHeaders, newRefreshToken);
        }

        if(!isTokenValid && isRefreshTokenValid) {
            newAccessToken = jwtService.generateToken(user);
            cookieService.addTokenCookie(JWT_COOKIE_ACCESS, responseHeaders, newAccessToken);

            newRefreshToken = jwtService.generateRefreshToken(user);
            cookieService.addTokenCookie(JWT_COOKIE_REFRESH, responseHeaders, newRefreshToken);
        }

        AuthenticationResponse authResponse = new AuthenticationResponse(AuthenticationResponse.Result.SUCCESS,
                "Auth successful. Tokens are created in cookie!");
        return ResponseEntity.ok().headers(responseHeaders).body(authResponse);
    }

    public ResponseEntity<AuthenticationResponse> refreshToken(String refreshToken) {
        HttpHeaders responseHeaders = new HttpHeaders();

        if (refreshToken != null && jwtService.isRefreshToken(refreshToken) && jwtService.isTokenValid(refreshToken)) {

            String username = jwtService.extractUsername(refreshToken);
            UserDetails user = userService.loadUserByUsername(username);

            Token newAccessToken = jwtService.generateToken(user);
            Token newRefreshToken = jwtService.generateRefreshToken(user);

            cookieService.addTokenCookie(JWT_COOKIE_ACCESS, responseHeaders, newAccessToken);
            cookieService.addTokenCookie(JWT_COOKIE_REFRESH, responseHeaders, newRefreshToken);

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(new AuthenticationResponse(
                            AuthenticationResponse.Result.SUCCESS,
                            "Tokens refreshed successfully"
                    ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthenticationResponse(
                        AuthenticationResponse.Result.FAILURE,
                        "Session expired. Please login again."
                ));
    }

    public ResponseEntity<AuthenticationResponse> logout() {
        HttpHeaders responseHeaders = new HttpHeaders();

        cookieService.deleteTokenCookie(JWT_COOKIE_ACCESS, responseHeaders);
        cookieService.deleteTokenCookie(JWT_COOKIE_REFRESH, responseHeaders);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(new AuthenticationResponse(
                        AuthenticationResponse.Result.SUCCESS,
                        "Logout successful!"
                ));
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
