package com.example.file_transfert.service;

import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.User;
import com.example.file_transfert.service.interfaces.IAccessControlService;
import com.example.file_transfert.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService implements IAccessControlService {
    @Autowired
    private IUserService userService;

    @Override
    public boolean hasPermission(String username, Permission permission) {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " not found.");
        }
        return user.getRoles()
                .stream()
                .anyMatch(
                        role -> role.getPermissions()
                                .stream()
                                .anyMatch(
                                        p -> p.equals(permission)
                                )
                );
    }
}
