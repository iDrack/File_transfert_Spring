package com.example.file_transfert.service;

import com.example.file_transfert.model.*;
import com.example.file_transfert.repository.FileResourceRepository;
import com.example.file_transfert.service.interfaces.IFileStorageService;

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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService implements IFileStorageService {

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
                              Resources.Visibility visibility) throws IOException {

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
        if (visibility == null) {
            visibility = Resources.Visibility.PRIVATE;
        }
        meta.setVisibility(visibility);

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
    public Set<String> getFilenamesByOwnerUsername(User owner) {
        return fileRepo.getFileResourcesByOwner(owner).stream().map(FileResource::getStorageName).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getPublicFilenames() {
        return fileRepo.getFileResources().stream()
                .filter(f -> f.getVisibility().equals(Resources.Visibility.PUBLIC))
                .map(FileResource::getStorageName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSharedFilename(String username) {
        Long userId = userService.getByUsername(username).getId();
        return fileRepo.getFileResourcesSharedWithUser(userId).stream().map(FileResource::getStorageName).collect(Collectors.toSet());
    }

    @Override
    public Resource loadAsResource(String storageFilename) throws IOException {
        Path file = root.resolve(storageFilename).normalize();

        if (!Files.exists(file) || !file.startsWith(root)) {
            throw new FileNotFoundException("File not found");
        }
        return new UrlResource(file.toUri());
    }

    @Override
    public void delete(String storageFilename) throws IOException {
        Files.deleteIfExists(root.resolve(storageFilename));
    }
}
