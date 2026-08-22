package com.example.api_oauth2_rbac.service.interfaces;

import com.example.api_oauth2_rbac.model.Resources;

public interface IResourcesService {

    Resources updateVisibility(Resources resource, Resources.Visibility newVisibility);

    Resources updateSharedUsers(Resources resource, String[] newUsers);

    Resources revokeSharingFromUsers(Resources resource, String[] usersToDelete);
}
