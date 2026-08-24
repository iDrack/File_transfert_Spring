package com.example.file_transfert.dto.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UserLogin {
    private String username;
    private String password;

    public String getUsername() {
        return username.trim();
    }

    public String getPassword() {
        return password.trim();
    }
}
