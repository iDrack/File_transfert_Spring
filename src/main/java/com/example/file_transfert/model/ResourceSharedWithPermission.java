package com.example.file_transfert.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resource_shared_with_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResourceSharedWithPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @Column(name = "permissions")
    private Set<Permission> permissions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", insertable = false, updatable = false)
    @JsonIgnore
    private Resources resource;

    public boolean isPermissionPresent(Permission permission) {
        if(! permission.toString().startsWith("RESOURCE_")) return false;
        return permissions.stream().anyMatch(p -> p.equals(permission));
    }

    public Set<Permission> replacePermissions(Set<Permission> newPermissions) {
        this.permissions = newPermissions;
        return this.permissions;
    }

    public Set<Permission> updatePermissions(Set<Permission> newPermissions) {
        newPermissions.forEach((newPermission -> {
            if (this.permissions.stream().noneMatch(p -> p == newPermission)) {
                this.permissions.add(newPermission);
            }
        }));
        return this.permissions;
    }

}
