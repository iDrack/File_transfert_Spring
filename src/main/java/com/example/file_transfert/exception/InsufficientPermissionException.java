package com.example.file_transfert.exception;

import com.example.file_transfert.model.Permission;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class InsufficientPermissionException extends HttpClientErrorException {
    public InsufficientPermissionException(Permission permissionRequired) {
        super(HttpStatusCode.valueOf(403), "You lack the required permission : " + permissionRequired);
    }
}
