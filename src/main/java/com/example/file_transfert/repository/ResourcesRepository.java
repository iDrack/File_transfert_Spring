package com.example.file_transfert.repository;

import com.example.file_transfert.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourcesRepository extends JpaRepository<Resources, Long> {
    Optional<Resources> findResourcesById(Long id);
}
