package com.bank.light.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UserEditDto {
    private Long userId;

    private String username;

    private String password;

    private String confirmPassword;

    private Boolean isUser = false;

    private Boolean isManager = false;

    private Boolean isAdmin = false;

    private Boolean isEnabled = false;
}
