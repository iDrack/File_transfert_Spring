package com.example.api_oauth2_rbac.repository;

import com.example.api_oauth2_rbac.model.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
    Optional<FileResource> getFileResourcesById(Long id);
}
