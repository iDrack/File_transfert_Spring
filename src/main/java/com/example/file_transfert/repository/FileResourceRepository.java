package com.example.file_transfert.repository;

import com.example.file_transfert.model.FileResource;
import com.example.file_transfert.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResource, Long> {

    Optional<FileResource> getFileResourcesById(Long id);

    Optional<FileResource> getFileResourcesByStorageName(String storageName);

    Set<FileResource> getFileResourcesByOwner(User owner);

    @Query(value = "SELECT f " +
            "FROM FileResource f " +
            "JOIN f.sharedWithUsers r " +
            "WHERE r.user.id = :userId")
    Set<FileResource> getFileResourcesSharedWithUser(Long userId);
}
