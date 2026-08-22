package com.example.api_oauth2_rbac.controller;

import com.example.api_oauth2_rbac.dto.file.FileResourceCreate;
import com.example.api_oauth2_rbac.model.FileResource;
import com.example.api_oauth2_rbac.model.User;
import com.example.api_oauth2_rbac.service.interfaces.IFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileTransferController {
    @Autowired
    private IFileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser,
            @RequestBody FileResourceCreate fileResourceCreateDto) throws IOException {

        // basic validation
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }
        // optional: check content type whitelist
        // if (!allowedTypes.contains(file.getContentType())) { ... }

        FileResource meta = fileStorageService.store(
                file,
                currentUser,
                fileResourceCreateDto.getVisibility(),
                fileResourceCreateDto.getSharedWithUsers()
        );

        return ResponseEntity.ok(Map.of(
                "id", meta.getId().toString(),
                "originalName", meta.getName(),
                "downloadUrl", "/api/files/download/" + meta.getStorageName()
        ));
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename) throws IOException {
        //TODO: Check if the user is authorized to download a file (Permission.RESOURCE_READ) (need to be in the shared user list of the resource or owner)
        Resource resource = fileStorageService.loadAsResource(filename);
        String contentType = Files.probeContentType(Paths.get(resource.getURI()));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
