package com.example.file_transfert.service;

import com.example.file_transfert.dto.user.UserCreate;
import com.example.file_transfert.dto.user.UserLogin;
import com.example.file_transfert.dto.user.UserUpdate;
import com.example.file_transfert.model.Role;
import com.example.file_transfert.model.User;
import com.example.file_transfert.repository.RoleRepository;
import com.example.file_transfert.repository.UserRepository;
import com.example.file_transfert.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService implements IUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private final Pattern passwordRegex = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$");

    @Override
    public User create(UserCreate userCreateDto) throws IllegalArgumentException {
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsUserByUsername(userCreateDto.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }
        User newUser = User.builder()
                .username(userCreateDto.getUsername())
                .email(userCreateDto.getEmail())
                .password(passwordEncoder.encode(userCreateDto.getPassword()))
                .active(true)
                .build();
        newUser.setRoles(roleRepository.findRoleByName("ROLE_USER").map(Set::of).orElseThrow(() -> new IllegalArgumentException("Default role not found")));

        if (userCreateDto.getFirstName() != null) {
            newUser.setFirstName(userCreateDto.getFirstName());
        }
        if (userCreateDto.getLastName() != null) {
            newUser.setLastName(userCreateDto.getLastName());
        }

        newUser = userRepository.save(newUser);
        return newUser;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User login(UserLogin userLoginDto) throws HttpClientErrorException {
        String name = userLoginDto.getUsername();
        if (name.isEmpty() || userLoginDto.getPassword().isEmpty()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(400), "Name and password are required.");
        }
        User user = getByUsername(name);
        if (user == null || !passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "Invalid username or password.");
        }
        if (!user.isActive()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "Account is disabled.");
        }
        return user;
    }

    @Override
    public boolean testCredentials(User user, String password) throws HttpClientErrorException {
        if (!user.isActive()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "Account is disabled.");
        }
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "Invalid password.");
        }
        return true;
    }


    @Override
    public User getByUsername(String username) {
        Optional<User> userFound = userRepository.findUserByUsername(username);
        return userFound.orElse(null);
    }

    @Override
    public User setAdmin(UserUpdate userUpdateDto) {
        if (userUpdateDto.getUsername() == null) {
            throw new IllegalArgumentException("User name is required");
        }
        User user = getByUsername(userUpdateDto.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setRoles(roleRepository.findRoleByName("ROLE_ADMIN").map(Set::of).orElseThrow(() -> new IllegalArgumentException("Admin role not found")));
        return userRepository.save(user);
    }

    @Override
    public User setRole(UserUpdate userUpdateDto, Set<Role> roles) {
        if (userUpdateDto.getUsername() == null) {
            throw new IllegalArgumentException("User name is required");
        }
        User user = getByUsername(userUpdateDto.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    public User addRole(UserUpdate userUpdateDto, Role role) {
        if (userUpdateDto.getUsername() == null) {
            throw new IllegalArgumentException("User name is required");
        }
        User user = getByUsername(userUpdateDto.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Override
    public boolean deleteByUsername(String username) {
        User user = getByUsername(username);
        if (user != null) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }

    @Override
    public User update(User user, UserUpdate userUpdateDto) throws HttpClientErrorException {
        if (userUpdateDto.getUsername() != null && !userUpdateDto.getUsername().isBlank()) {
            if (userRepository.findUserByUsername(userUpdateDto.getUsername()).isPresent()) {
                throw new HttpClientErrorException(HttpStatusCode.valueOf(400), "Username already in use");
            } else {
                user.setUsername(userUpdateDto.getUsername());
            }
        }
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isBlank()) {
            if (userRepository.findUserByEmail(userUpdateDto.getEmail()).isPresent()) {
                throw new HttpClientErrorException(HttpStatusCode.valueOf(400), "Email already in use");
            } else {
                user.setEmail(userUpdateDto.getEmail());
            }
        }
        if (userUpdateDto.getFirstName() != null) {
            user.setFirstName(userUpdateDto.getFirstName());
        }
        if (userUpdateDto.getLastName() != null) {
            user.setLastName(userUpdateDto.getLastName());
        }
        if (userUpdateDto.getNewPassword() != null) {
            Matcher passMatcher = userUpdateDto.getNewPassword() != null ? passwordRegex.matcher(userUpdateDto.getNewPassword()) : null;
            if (passMatcher != null && !passMatcher.matches()) {
                user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
            } else {
                throw new HttpClientErrorException(HttpStatusCode.valueOf(400), "Password does not meet the required criteria.");
            }
        }
        return userRepository.save(user);
    }

    @Override
    public User disableAccount(String username) throws HttpClientErrorException{
        User user = getByUsername(username);
        if (user == null) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(404), "User not found.");
        } else if (! user.isActive()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(400), user.getUsername() + "'s account is already disabled.");
        }
        user.setActive(false);
        return userRepository.save(user);
    }
}
