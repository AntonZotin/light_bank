package com.bank.light.models;

import com.bank.light.security.validators.PasswordMatches;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatches
public class UserDto {
    @NotNull(message = "Username can't be null")
    @NotEmpty(message = "Username can't be empty")
    private String username;

    @NotNull(message = "Password can't be null")
    @NotEmpty(message = "Password can't be empty")
    private String password;

    @NotNull(message = "MatchingPassword can't be null")
    @NotEmpty(message = "MatchingPassword can't be empty")
    private String matchingPassword;
}
