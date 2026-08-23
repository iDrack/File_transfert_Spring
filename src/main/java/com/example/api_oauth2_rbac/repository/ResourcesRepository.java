package com.example.api_oauth2_rbac.repository;

import com.example.api_oauth2_rbac.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourcesRepository extends JpaRepository<Resources, Long> {
    Optional<Resources> findResourcesById(Long id);
}
