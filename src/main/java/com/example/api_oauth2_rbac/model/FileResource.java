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

    //TODO: Add Annotation to check if a user is in the shared user list of a resource

    //name correspond to original filename
    @Column(name = "storage_name", nullable = false, length = 255)
    @EqualsAndHashCode.Include
    private String storageName;

    @Column(name = "size", nullable = false, length = 100)
    private long size;

    @Column(name = "mime_type", nullable = false, length = 255)
    private String mimeType;

    public FileResource() {
        super();
        setType("file");
    }

    public FileResource(String storageName, Long size, String mimeType) {
        super();
        this.storageName = storageName;
        this.mimeType = mimeType;
        setType("file");
    }

    public FileResource(Long id, String originalName, String type, Long ownerIs, User owner, LocalDateTime createdAt, Visibility visibility, Set<ResourceSharedWithPermission> sharedWithUsers, long size, String storageName, String mimeType) {
        super(id, originalName, type, ownerIs, owner, createdAt, visibility, sharedWithUsers);
        this.size = size;
        this.storageName = storageName;
        this.mimeType = mimeType;
    }
}
