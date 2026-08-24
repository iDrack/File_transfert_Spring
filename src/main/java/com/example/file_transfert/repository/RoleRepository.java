package com.example.file_transfert.repository;

import com.example.file_transfert.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Boolean existsRoleByName(String name);
    Optional<Role> findRoleByName(String name);
}
