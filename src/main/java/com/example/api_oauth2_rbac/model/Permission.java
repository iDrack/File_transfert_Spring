package com.example.api_oauth2_rbac.model;

import jakarta.persistence.*;
import lombok.*;


public enum Permission {
    ADMIN_ACCESS,
    USER_CREATE,
    USER_DELETE,
    USER_DELETE_SELF,
    USER_UPDATE,
    USER_UPDATE_SELF,
    USER_READ,
    RESOURCE_READ,
    RESOURCE_DELETE,
    RESOURCE_UPDATE,
    RESOURCE_CREATE,
    RESOURCE_MANAGE_USERS,
    RESOURCE_MANAGE_VISIBILITY,
}
