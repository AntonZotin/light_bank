package com.bank.light.models;

import lombok.Getter;
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

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public boolean isManager() {
        return this.isManager;
    }

    public boolean isUser() {
        return this.isUser;
    }
}
