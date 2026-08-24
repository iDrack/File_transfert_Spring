package com.example.file_transfert.service.interfaces;

import com.example.file_transfert.exception.InsufficientPermissionException;
import com.example.file_transfert.model.Permission;


public interface IAccessControlService {
    public boolean hasPermission(String username, Permission permission) throws InsufficientPermissionException;

}
