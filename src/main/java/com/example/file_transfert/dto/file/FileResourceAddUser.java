package com.example.file_transfert.dto.file;

import com.example.file_transfert.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResourceAddUser {
    String username;
    Set<Permission> permissions;
}
