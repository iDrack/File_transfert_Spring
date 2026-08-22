package com.example.api_oauth2_rbac.service;

import com.example.api_oauth2_rbac.model.*;
import com.example.api_oauth2_rbac.repository.FileResourceRepository;
import com.example.api_oauth2_rbac.service.interfaces.IFileStorageService;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService implements IFileStorageService {
    //TODO: Add service to share a file with a user

    //Files will be stored at /uploads
    private final Path root = Paths.get("uploads");

    @Autowired
    private UserService userService;

    @Autowired
    private FileResourceRepository fileRepo;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root);
    }

    @Override
    public FileResource store(MultipartFile file,
                              User user,
                              Resources.Visibility visibility,
                              Map<String, Set<Permission>> sharedWithUsers) throws IOException {

        String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (original.contains("..")) {
            throw new IOException("Invalid path in filename.");
        }

        String ext = StringUtils.getFilenameExtension(original);
        String uuid = UUID.randomUUID().toString();
        String storageName = uuid + (ext != null ? "." + ext : "");

        Path target = root.resolve(storageName).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("Cannot store file outside root directory");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        FileResource meta = new FileResource();
        meta.setName(original);
        meta.setStorageName(storageName);
        meta.setMimeType(file.getContentType());
        meta.setSize(file.getSize());
        meta.setOwner(user);
        meta.setOwnerId(user.getId());
        if (visibility == null) {
            visibility = Resources.Visibility.PRIVATE;
        }
        meta.setVisibility(visibility);

        if (sharedWithUsers != null) {
            sharedWithUsers.forEach((username, permissions) -> meta.getSharedWithUsers().add(
                    ResourceSharedWithPermission.builder()
                            .user(userService.getByUsername(username))
                            .permissions(permissions)
                            .build()
            ));
        }

        return fileRepo.save(meta);
    }

    @Override
    public FileResource getFileResourceByStorageName(String storageName) {
        return fileRepo.getFileResourcesByStorageName(storageName).orElseThrow(
                () -> new RuntimeException("File " + storageName + " is missing.")
        );
    }

    @Override
    public FileResource getFileResourceById(Long id) {
        return fileRepo.getFileResourcesById(id).orElseThrow(
                () -> new RuntimeException("File with id: " + id + " is missing.")
        );
    }

    @Override
    public Resource loadAsResource(String storageFilename) throws IOException {
        Path file = root.resolve(storageFilename).normalize();
        if (!Files.exists(file) || file.startsWith(root)) {
            throw new FileNotFoundException("File not found");
        }
        return new UrlResource(file.toUri());
    }

    @Override
    public void delete(String storageFilename) throws IOException {
        Files.deleteIfExists(root.resolve(storageFilename));
    }
}
