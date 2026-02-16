package com.grosso.chat.authentication;

import com.grosso.chat.model.Role;
import com.grosso.chat.validator.PasswordMatching;
import com.grosso.chat.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatching.List({
        @PasswordMatching(
                password = "password",
                repeatedPassword = "repeatedPassword",
                message = "Passwords do not match!"
        )
})
public class RegistrationRequest {
    @NotEmpty(message = "Email shouldn't be null")
    @Email(message = "Invalid format email!")
    private String email;

    @NotEmpty(message = "First name shouldn't be null")
    private String firstName;

    @NotEmpty(message = "Last name shouldn't be null")
    private String lastName;

    @NotEmpty(message = "Username shouldn't be null")
    private String username;

    @ValidPassword
    private String password;

    private String role;

    @NotEmpty(message = "Repeat password shouldn't be null")
    private String repeatedPassword;
}
