package com.example.file_transfert.controller;

import com.example.file_transfert.model.FileResource;
import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.Resources;
import com.example.file_transfert.model.User;
import com.example.file_transfert.security.annotation.IsSharedWithActiveUser;
import com.example.file_transfert.service.interfaces.IFileStorageService;
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
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileTransferController {
    @Autowired
    private IFileStorageService fileStorageService;

    @PostMapping(value = "/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "visibility", required = false) Resources.Visibility visibility,
            @AuthenticationPrincipal User currentUser)
            throws IOException {

        // basic validation
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }
        // optional: check content type whitelist
        // if (!allowedTypes.contains(file.getContentType())) { ... }

        if (visibility == null) {
            visibility = Resources.Visibility.PRIVATE;
        }

        FileResource meta = fileStorageService.store(
                file,
                currentUser,
                visibility
        );

        return ResponseEntity.ok(Map.of(
                "id", meta.getId().toString(),
                "originalName", meta.getName(),
                "downloadUrl", "/api/files/download/" + meta.getStorageName()
        ));
    }

    @GetMapping("/download/{filename:.+}")
    @PreAuthorize("isAuthenticated()")
    @IsSharedWithActiveUser(permission = Permission.RESOURCE_READ)
    public ResponseEntity<Resource> download(@PathVariable String filename) throws IOException {
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

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String,String>> getUserFilenames(@AuthenticationPrincipal User currentUser) {

    }
}
