package com.example.file_transfert.service;

import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.ResourceSharedWithPermission;
import com.example.file_transfert.model.Resources;
import com.example.file_transfert.repository.ResourcesRepository;
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

    /**
     * Find aresource with its ID
     * @param id id to look up
     * @return resource corresponding to the id
     */
    @Override
    public Resources findResourceById(Long id) {
        return resourcesRepository.findResourcesById(id).orElseThrow(
                () -> new RuntimeException("Resource with id: " + id + "is missing."));
    }

    /**
     * Change the visibility of a resource
     * @param resource resource to update
     * @param newVisibility new visibility
     * @return updated resource
     */
    @Override
    public Resources updateVisibility(Resources resource, Resources.Visibility newVisibility) {
        resource.setVisibility(newVisibility);
        resourcesRepository.save(resource);
        return resource;
    }

    /**
     * Share a resource with new users
     * @param resource resource to share
     * @param newUsers new suers to add to the sharing list
     * @param permissions permissions for the newly added users
     * @return updated resource
     */
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

    /**
     * Share a resource with new users using a list of permissions, if a user is already present, replace its permissions
     * @param resource resource to update
     * @param newUsers map of new users with their permissions
     * @return resource updated
     */
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

    /**
     * Replace a user set of permissions with new one on a resource the user has access
     * @param resource resource to update
     * @param username username of the user to update
     * @param newPermissions new permission for the user
     * @return updated resource
     */
    @Override
    public Resources replaceUserPermission(Resources resource, String username, Set<Permission> newPermissions) {
        ResourceSharedWithPermission userPermission = resource.findUser(username);
        if (userPermission != null) {
            userPermission.replacePermissions(newPermissions);
            resourcesRepository.save(resource);
        }
        return resource;
    }

    /**
     * Add new permissions to a user having access to a resource
     * @param resource resource to update
     * @param username username of the user to update
     * @param newPermissions permissions to add to the user on the specified resource
     * @return updated resource
     */
    @Override
    public Resources addUserPermissions(Resources resource, String username, Set<Permission> newPermissions) {
        ResourceSharedWithPermission userPermission = resource.findUser(username);
        if (userPermission != null) {
            userPermission.updatePermissions(newPermissions);
        }
        return resource;
    }

    /**
     * Revoke a permission from a user having access to a resource
     * @param resource resource to update
     * @param username username of the user to update its permissions
     * @param permission permission to revoke from the user
     * @return updated resource
     */
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

    /**
     * Remove user access to a resource.
     * @param resource resource to update
     * @param usersToDelete user to revoke
     * @return updated resource
     */
    @Override
    public Resources revokeSharingFromUsers(Resources resource, Set<String> usersToDelete) {
        resource.getSharedWithUsers().removeIf(sharedUser ->
                usersToDelete.contains(sharedUser.getUser().getUsername())
        );
        return resource;
    }

    /**
     * Change ownership of a file to the user with specified username
     * @param resource resource to transfert
     * @param username new owner's username
     * @return updated resource
     */
    @Override
    public Resources changeOwnership(Resources resource, String username) {
        if (!resource.getOwner().getUsername().equals(username)) {
            resource.setOwner(userService.getByUsername(username));
            resourcesRepository.save(resource);
        }
        return resource;
    }
}
