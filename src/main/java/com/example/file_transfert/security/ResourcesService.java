package com.example.file_transfert.security;

import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.ResourceSharedWithPermission;
import com.example.file_transfert.model.Resources;
import com.example.file_transfert.model.User;
import com.example.file_transfert.repository.ResourcesRepository;
import com.example.file_transfert.service.UserService;
import com.example.file_transfert.service.interfaces.IResourcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ResourcesService implements IResourcesService {
    @Autowired
    private ResourcesRepository resourcesRepository;

    @Autowired
    private UserService userService;

    @Override
    public Resources findResourceById(Long id) {
        return resourcesRepository.findResourcesById(id).orElseThrow(
                () -> new RuntimeException("Resource with id: " + id + "is missing."));
    }

    @Override
    public Resources updateVisibility(Resources resource, Resources.Visibility newVisibility) {
        resource.setVisibility(newVisibility);
        resourcesRepository.save(resource);
        return resource;
    }

    @Override
    public Resources sharedWithNewUsers(Resources resource, Set<String> newUsers, Set<Permission> permissions) {
        //If User already in sharing group, ignore it
        Set<ResourceSharedWithPermission> oldUsers = resource.getSharedWithUsers();

        newUsers.forEach((newUser) -> {
            if (oldUsers.stream().noneMatch(oldUser -> oldUser.getUser().getUsername().equals(newUser))) {
                if (userService.getByUsername(newUser) != null) {
                    ResourceSharedWithPermission sharedUser = ResourceSharedWithPermission.builder()
                            .user(userService.getByUsername(newUser))
                            .resource(resource)
                            .permissions(permissions)
                            .build();
                    resource.getSharedWithUsers().add(sharedUser);
                }
            }
        });
        resourcesRepository.save(resource);
        return resource;
    }

    @Override
    public Resources updateSharedUsers(Resources resource, Map<String, Set<Permission>> newUsers) {
        Map<String, ResourceSharedWithPermission> existingByUsername = resource.getSharedWithUsers().stream()
                .collect(Collectors.toMap(
                        sharedUser -> sharedUser.getUser().getUsername(),
                        Function.identity(),
                        (a, b) -> a,
                        HashMap::new
                ));

        newUsers.forEach((username, permissions) -> {
            ResourceSharedWithPermission existing = existingByUsername.get(username);
            //Already shared with user -> Replace its permissions
            if (existing != null) {
                existing.replacePermissions(permissions);
            } else {
                if (userService.getByUsername(username) != null) {
                    resource.getSharedWithUsers().add(
                            ResourceSharedWithPermission.builder()
                                    .user(userService.getByUsername(username))
                                    .resource(resource)
                                    .permissions(permissions)
                                    .build()
                    );
                }
            }
        });

        resourcesRepository.save(resource);
        return resource;
    }

    @Override
    public Resources replaceUserPermission(Resources resource, String username, Set<Permission> newPermissions) {
        ResourceSharedWithPermission userPermission = resource.findUser(username);
        if (userPermission != null) {
            userPermission.replacePermissions(newPermissions);
            resourcesRepository.save(resource);
        }
        return resource;
    }

    @Override
    public Resources addUserPermission(Resources resource, String username, Permission permission) {
        ResourceSharedWithPermission userPermission = resource.findUser(username);
        if (userPermission != null) {
            if (userPermission.getPermissions().stream().noneMatch(p -> p.equals(permission)))
                userPermission.getPermissions().add(permission);
            resourcesRepository.save(resource);
        }
        return resource;
    }

    @Override
    public Resources revokeUserPermission(Resources resource, String username, Permission permission) {
        ResourceSharedWithPermission userPermission = resource.findUser(username);
        if (userPermission != null) {
            if (userPermission.getPermissions().stream().anyMatch(p -> p.equals(permission)))
                userPermission.getPermissions().removeIf(p -> p.equals(permission));
            resourcesRepository.save(resource);
        }
        return resource;
    }

    @Override
    public Resources revokeSharingFromUsers(Resources resource, Set<String> usersToDelete) {
        resource.getSharedWithUsers().removeIf(sharedUser ->
                usersToDelete.contains(sharedUser.getUser().getUsername())
        );
        return resource;
    }
}
