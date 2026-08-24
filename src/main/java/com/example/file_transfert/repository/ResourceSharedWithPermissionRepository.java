package com.example.file_transfert.repository;

import com.example.file_transfert.model.ResourceSharedWithPermission;
import com.example.file_transfert.model.Resources;
import com.example.file_transfert.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ResourceSharedWithPermissionRepository extends JpaRepository<ResourceSharedWithPermission, Long> {
    Optional<Set<ResourceSharedWithPermission>> findResourceSharedWithPermissionsByUser(User user);

    Optional<Set<ResourceSharedWithPermission>> findResourceSharedWithPermissionsByResource(Resources resources);

}
