package com.example.api_oauth2_rbac.service.interfaces;

import com.example.api_oauth2_rbac.model.FileResource;
import com.example.api_oauth2_rbac.model.Permission;
import com.example.api_oauth2_rbac.model.Resources;
import com.example.api_oauth2_rbac.model.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface IFileStorageService {
    FileResource store(MultipartFile file,
                       User user,
                       Resources.Visibility visibility) throws IOException;

    FileResource getFileResourceByStorageName(String storageName);

    FileResource getFileResourceById(Long id);

    Resource loadAsResource(String storageFilename) throws IOException;

    void delete(String storageFilename) throws IOException;
}
