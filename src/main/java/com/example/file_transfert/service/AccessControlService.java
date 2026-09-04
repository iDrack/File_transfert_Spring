package com.example.file_transfert.service;

import com.example.file_transfert.exception.InsufficientPermissionException;
import com.example.file_transfert.model.*;
import com.example.file_transfert.service.interfaces.IAccessControlService;
import com.example.file_transfert.service.interfaces.IFileStorageService;
import com.example.file_transfert.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Set;

@Service
public class AccessControlService implements IAccessControlService {
    @Autowired
    private IUserService userService;

    private IFileStorageService storage;

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

    @Override
    public boolean isFileSharedWith(String username, String filename, Permission permission) throws InsufficientPermissionException, AccessDeniedException {
        FileResource fileResource = storage.getFileResourceByStorageName(filename);
        //If public, accept access
        if (fileResource.getVisibility().equals(Resources.Visibility.PUBLIC)) return true;
        //If user is owner, accept access
        if (fileResource.getOwner().getUsername().equals(username)) return true;
        //Search if the user is in the list of people in which the file is shared with
        //If absent, reject access
        Set<ResourceSharedWithPermission> resources = fileResource.getSharedWithUsers();
        ResourceSharedWithPermission res = resources.stream()
                .filter(r -> r.getUser().getUsername().equals(username))
                .findFirst().orElseThrow(() -> new AccessDeniedException("This file is not available to you"));
        //Check if user has sufficient permission
        return isResourceSharedWith(username, res, permission);
    }

    @Override
    public boolean isResourceSharedWith(String username, ResourceSharedWithPermission res, Permission permission) throws InsufficientPermissionException {

        if (!res.getUser().getUsername().equals(username)) {
            throw new InsufficientPermissionException(permission);
        }

        if (res.getResource().getVisibility().equals(Resources.Visibility.READONLY) && !permission.equals(Permission.RESOURCE_READ)) {
            throw new InsufficientPermissionException(Permission.RESOURCE_READ);
        }

        if (res.getPermissions().stream().noneMatch(p -> p.equals(permission))) {
            return true;
        }
        return false;
    }

    @Override
    public void checkUserOwnership(String username, String filename) throws HttpClientErrorException {
        FileResource file = storage.getFileResourceByStorageName(filename);
        User user = userService.getByUsername(username);
        if (!user.equals(file.getOwner())) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "Only the owner can execute this action.");
        }
    }
}
