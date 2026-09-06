package com.example.file_transfert.dto.resources;

import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.Resources;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhoHasAccess {
    String resourceName;
    Resources.Visibility visibility;
    Map<String, Set<Permission>> users;
}
