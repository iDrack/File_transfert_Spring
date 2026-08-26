package com.example.file_transfert.service.interfaces;

import com.example.file_transfert.model.FileResource;
import com.example.file_transfert.model.Resources;
import com.example.file_transfert.model.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface IFileStorageService {
    FileResource store(MultipartFile file,
                       User user,
                       Resources.Visibility visibility) throws IOException;

    FileResource getFileResourceByStorageName(String storageName);

    FileResource getFileResourceById(Long id);

    Set<String> getFilenamesByOwnerUsername(User owner);

    Set<String> getPublicFilenames();

    Set<String> getSharedFilename(String username);

    Resource loadAsResource(String storageFilename) throws IOException;

    void delete(String storageFilename) throws IOException;
}
