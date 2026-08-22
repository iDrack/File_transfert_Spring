package com.example.api_oauth2_rbac.dto.file;

import com.example.api_oauth2_rbac.model.Permission;
import com.example.api_oauth2_rbac.model.Resources;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResourceCreate {
    Resources.Visibility visibility;
    Map<String, Set<Permission>> sharedWithUsers; //username for key, permissions for value
}
