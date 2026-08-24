package com.example.file_transfert.service.interfaces;

import com.example.file_transfert.dto.user.UserCreate;
import com.example.file_transfert.dto.user.UserLogin;
import com.example.file_transfert.dto.user.UserUpdate;
import com.example.file_transfert.model.Role;
import com.example.file_transfert.model.User;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Set;

public interface IUserService {
    public User create(UserCreate userCreateDto) throws IllegalArgumentException;

    public List<User> getAllUsers();

    public User login(UserLogin userLoginDto)throws HttpClientErrorException;

    public boolean testCredentials(User user, String password) throws HttpClientErrorException;

    public User getByUsername(String username);

    public User setAdmin(UserUpdate userUpdateDto);

    public User setRole(UserUpdate userUpdateDto, Set<Role> roles);

    public User addRole(UserUpdate userUpdateDto, Role role);

    public boolean deleteByUsername(String username);

    User update(User user, UserUpdate userUpdateDto) throws HttpClientErrorException;

    User disableAccount(String username) throws HttpClientErrorException;
}
