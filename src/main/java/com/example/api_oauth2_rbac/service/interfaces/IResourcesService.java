package com.example.api_oauth2_rbac.service.interfaces;

import com.example.api_oauth2_rbac.model.Permission;
import com.example.api_oauth2_rbac.model.Resources;

import java.util.Map;
import java.util.Set;

public interface IResourcesService {

    Resources findResourceById(Long id);

    Resources updateVisibility(Resources resource, Resources.Visibility newVisibility);

    Resources sharedWithNewUsers(Resources resource, Set<String> newUsers, Set<Permission> permissions);

    Resources updateSharedUsers(Resources resource, Map<String, Set<Permission>> newUsers);

    Resources revokeSharingFromUsers(Resources resource, Set<String> usersToDelete);
}
