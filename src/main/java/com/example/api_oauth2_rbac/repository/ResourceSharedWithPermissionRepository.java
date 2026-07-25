package com.example.api_oauth2_rbac.repository;

import com.example.api_oauth2_rbac.model.ResourceSharedWithPermission;
import com.example.api_oauth2_rbac.model.Resources;
import com.example.api_oauth2_rbac.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ResourceSharedWithPermissionRepository extends JpaRepository<ResourceSharedWithPermission, Long> {
    Optional<Set<ResourceSharedWithPermission>> findResourceSharedWithPermissionsByUser(User user);

    Optional<Set<ResourceSharedWithPermission>> findResourceSharedWithPermissionsByResource(Resources resources);

}
