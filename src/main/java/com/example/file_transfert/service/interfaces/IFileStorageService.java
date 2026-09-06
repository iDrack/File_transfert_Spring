package com.example.file_transfert.service.interfaces;

import com.example.file_transfert.dto.file.FileResourceMetadata;
import com.example.file_transfert.dto.file.FileResourceMetadataSet;
import com.example.file_transfert.model.FileResource;
import com.example.file_transfert.model.Resources;
import com.example.file_transfert.model.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface IFileStorageService {
    FileResource store(MultipartFile file,
                       User user,
                       Resources.Visibility visibility) throws IOException;

    FileResource getFileResourceByStorageName(String storageName);

    FileResource getFileResourceById(Long id);

    FileResourceMetadataSet generateMetadata(List<FileResourceMetadata> files, int page);

    List<FileResourceMetadata> getFilesMetaByOwner(User owner, int page);

    List<FileResourceMetadata> getPublicFileMeta(User activeUser, int page);

    List<FileResourceMetadata> getSharedFileMeta(String username, int page);

    Resource loadAsResource(String storageFilename) throws IOException;

    void delete(String storageFilename) throws IOException;
}
