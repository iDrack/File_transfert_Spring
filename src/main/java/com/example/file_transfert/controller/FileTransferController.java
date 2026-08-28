package com.example.file_transfert.controller;

import com.example.file_transfert.dto.file.FileResourceAddUser;
import com.example.file_transfert.dto.file.FileResourceAddUsers;
import com.example.file_transfert.dto.file.FileResourceMetadata;
import com.example.file_transfert.dto.file.FileResourceMetadataSet;
import com.example.file_transfert.model.FileResource;
import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.Resources;
import com.example.file_transfert.model.User;
import com.example.file_transfert.security.annotation.IsSharedWithActiveUser;
import com.example.file_transfert.service.interfaces.IFileStorageService;
import com.example.file_transfert.service.interfaces.IResourcesService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
public class FileTransferController {
    @Autowired
    private IFileStorageService fileStorageService;
    @Autowired
    private IResourcesService resourcesService;

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


    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileResourceMetadataSet> getUserFilenames(
            @AuthenticationPrincipal User currentUser,
            @RequestParam int page
    ) {
        if (page <= 0) page = 1;
        List<FileResourceMetadata> files = fileStorageService.getFilesMetaByOwner(currentUser, page);
        return ResponseEntity.ok(fileStorageService.generateMetadata(files, page));
    }

    @GetMapping("/public")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileResourceMetadataSet> getPublicFilename(@RequestParam int page) {
        if (page <= 0) page = 1;
        List<FileResourceMetadata> files = fileStorageService.getPublicFileMeta(page);
        return ResponseEntity.ok(fileStorageService.generateMetadata(files, page));
    }

    @GetMapping("/shared")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileResourceMetadataSet> getSharedFilename(
            @AuthenticationPrincipal User currentUser,
            @RequestParam int page) {
        if (page <= 0) page = 1;
        List<FileResourceMetadata> files = fileStorageService.getSharedFileMeta(currentUser.getUsername(), page);
        return ResponseEntity.ok(fileStorageService.generateMetadata(files, page));
    }

    @PutMapping("/share-with-user/{filename}")
    @PreAuthorize("isAuthenticated()")
    @IsSharedWithActiveUser(permission = Permission.RESOURCE_MANAGE_USERS)
    public ResponseEntity<String> addUserToSharedList(
            @PathVariable String filename,
            @RequestBody FileResourceAddUser fileResourceAddUserDto
    ) {
        FileResource file = fileStorageService.getFileResourceByStorageName(filename);
        if (file == null) {
            ResponseEntity.status(404).body(Map.of("error", "File: " + filename + " not found"));
        }
        Map<String, Set<Permission>> userSetToMap = Map.of(fileResourceAddUserDto.getUsername(), fileResourceAddUserDto.getPermissions());
        FileResource updatedFile = (FileResource) resourcesService.updateSharedUsers(file, userSetToMap);

        return ResponseEntity.ok("File: " + filename + " is now shared with " + fileResourceAddUserDto.getUsername());
    }

    @PutMapping("/share-with-users/{filename}")
    @PreAuthorize("isAuthenticated()")
    @IsSharedWithActiveUser(permission = Permission.RESOURCE_MANAGE_USERS)
    public ResponseEntity<String> addUsersToSharedList(
            @PathVariable String filename,
            @RequestBody FileResourceAddUsers fileResourceAddUsersDto
    ) {
        FileResource file = fileStorageService.getFileResourceByStorageName(filename);
        if (file == null) {
            ResponseEntity.status(404).body(Map.of("error", "File: " + filename + " not found"));
        }
        Map<String, Set<Permission>> userSetToMap = new java.util.HashMap<>(Map.of());
        fileResourceAddUsersDto.getUsernames().forEach((username) -> {
            userSetToMap.put(username, fileResourceAddUsersDto.getPermissions());
        });

        FileResource updatedFile = (FileResource) resourcesService.updateSharedUsers(file, userSetToMap);

        return ResponseEntity.ok("File: " + filename + " is now shared with requested users");
    }

    @DeleteMapping("/revoke-sharing/{filename}")
    @PreAuthorize("isAuthenticated()")
    @IsSharedWithActiveUser(permission = Permission.RESOURCE_MANAGE_USERS)
    public ResponseEntity<String> removeUserFromSharedList(@PathVariable String filename, @RequestBody Set<String> users) {
        FileResource file = fileStorageService.getFileResourceByStorageName(filename);
        if (file == null) {
            ResponseEntity.status(404).body(Map.of("error", "File: " + filename + " not found"));
        }
        resourcesService.revokeSharingFromUsers(file, users);

        return ResponseEntity.ok("File: " + filename + " is no longer shared with requested users");
    }

    //TODO: Endpoint pour révoquer une permission sur un fichier pour un utilisateur
    //TODO: Endpoint pour ajouter une permission sur  un fichier à un utilisateur
    //TODO: Endpoint pour modifier la liste des permissions d'un utilisateur sur un fichier
}
