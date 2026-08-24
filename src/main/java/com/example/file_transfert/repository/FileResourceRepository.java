package com.example.file_transfert.repository;

import com.example.file_transfert.model.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
    Optional<FileResource> getFileResourcesById(Long id);
    Optional<FileResource> getFileResourcesByStorageName(String storageName);
}
