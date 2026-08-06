package com.example.api_oauth2_rbac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "file_resources")
@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class FileResource extends Resources{

    //TODO: Add service to transfert a file and create a resource with visibility
    //TODO: Add service to share a file with a list of users with permissions
    //TODO: Add a service to share a file one user and with permission
    //TODO: Add a service to update a resource data (visibility, shared with users, permission to user and default permission)
    //TODO: Add Annotation to check if a user is in the shared user list of a resource

    @Column(name = "original_name", nullable = false, length = 50)
    private String originalName;

    @Column(name = "location_path", nullable = false, length = 255)
    @EqualsAndHashCode.Include
    private String locationPath;

    @Column(name = "mime_type", nullable = false, length = 255)
    private String mimeType;

    public FileResource() {
        super();
        setType("file");
    }

    public FileResource(String originalName, String locationPath, String mimeType) {
        super();
        this.originalName = originalName;
        this.locationPath = locationPath;
        this.mimeType = mimeType;
        setType("file");
    }

    public FileResource(Long id, String name, String type, Long ownerIs, User owner, LocalDateTime createdAt, Visibility visibility, Set<ResourceSharedWithPermission> sharedWithUsers, String originalName, String locationPath, String mimeType) {
        super(id, name, type, ownerIs, owner, createdAt, visibility, sharedWithUsers);
        this.originalName = originalName;
        this.locationPath = locationPath;
        this.mimeType = mimeType;
    }
}
