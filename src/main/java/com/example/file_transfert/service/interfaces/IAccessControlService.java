package com.example.file_transfert.service.interfaces;

import com.example.file_transfert.exception.InsufficientPermissionException;
import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.ResourceSharedWithPermission;


public interface IAccessControlService {
    public boolean hasPermission(String username, Permission permission) throws InsufficientPermissionException;

    public boolean isFileSharedWith(String username, String filename, Permission permission) throws InsufficientPermissionException;

    public boolean isResourceSharedWith(String username, ResourceSharedWithPermission res, Permission permission) throws InsufficientPermissionException;
}
