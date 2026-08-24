package com.example.file_transfert.init;

import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.Role;
import com.example.file_transfert.model.User;
import com.example.file_transfert.repository.RoleRepository;
import com.example.file_transfert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_PASS}")
    private String adminPass;

    @Override
    public void run(String... args) throws Exception {

        // Créer les rôles
        if (roleRepository.findRoleByName("ROLE_USER").isEmpty()) {
            Set<Permission> userPerms = Set.of(
                    Permission.USER_READ,
                    Permission.USER_DELETE_SELF,
                    Permission.USER_UPDATE_SELF,
                    Permission.RESOURCE_CREATE
            );

            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .description("Utilisateur standard")
                    .permissions(userPerms)
                    .build();
            roleRepository.save(userRole);
        }

        if (roleRepository.findRoleByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrateur")
                    .permissions(Set.of(Permission.values()))
                    .build();
            roleRepository.save(adminRole);
        }

        // Créer un utilisateur admin par défaut
        if (userRepository.findUserByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode(adminPass))
                    .firstName("Admin")
                    .lastName("System")
                    .active(true)
                    .roles(Set.of(roleRepository.findRoleByName("ROLE_ADMIN").orElseThrow()))
                    .build();
            userRepository.save(admin);
        }
    }
}
