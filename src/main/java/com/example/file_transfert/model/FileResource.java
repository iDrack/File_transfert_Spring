package com.example.file_transfert.model;

import com.example.file_transfert.dto.file.FileResourceMetadata;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "file_resources")
@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class FileResource extends Resources {

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

    public FileResource(Long id, String originalName, String type, User owner, LocalDateTime createdAt, Visibility visibility, Set<ResourceSharedWithPermission> sharedWithUsers, long size, String storageName, String mimeType) {
        super(id, originalName, type, owner, createdAt, visibility, sharedWithUsers);
        this.size = size;
        this.storageName = storageName;
        this.mimeType = mimeType;
    }

    public FileResourceMetadata toMetaData(Long userId) {
        Set<Permission> permissions = new HashSet<>();
        if (userId != null) {
            if (Objects.equals(userId, this.getOwner().getId())) {
                permissions.addAll(List.of(
                        Permission.RESOURCE_READ,
                        Permission.RESOURCE_UPDATE,
                        Permission.RESOURCE_DELETE,
                        Permission.RESOURCE_MANAGE_USERS,
                        Permission.RESOURCE_MANAGE_VISIBILITY));
            } else {
                permissions = this.getSharedWithUsers().stream()
                        .filter(r -> Objects.equals(r.getUser().getId(), userId)).findFirst().get().getPermissions();
            }
        }

        if (this.getVisibility().equals(Visibility.PUBLIC) && !permissions.contains(Permission.RESOURCE_READ))
            permissions.add(Permission.RESOURCE_READ);
        return new FileResourceMetadata(this.storageName, this.getName(), this.mimeType, this.getOwner().getUsername(), this.size, permissions, this.getVisibility());
    }
}
