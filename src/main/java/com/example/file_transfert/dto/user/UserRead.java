package com.example.file_transfert.dto.user;

import com.example.file_transfert.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserRead {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    @Getter
    private Set<Role> roles;

    public String getUsername() {
        return username.trim();
    }

    public String getFirstName() {
        return firstName.trim();
    }

    public String getLastName() {
        return lastName.trim();
    }

    public String getEmail() {
        return email.trim();
    }

}
