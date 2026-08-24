package com.example.file_transfert.controller;

import com.example.file_transfert.model.Permission;
import com.example.file_transfert.security.annotation.RequirePermission;
import com.example.file_transfert.security.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/test", produces = "application/json")
public class TestController {

    @Autowired
    private JwtService jwtService;

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        return ResponseEntity.ok(Map.of("data", "This is a public endpoint"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @RequirePermission(Permission.ADMIN_ACCESS)
    public ResponseEntity<Map<String, String>> adminOnlyEndpoint(
            @RequestHeader(value = "Authorization", required = true) String authToken
    ) {
        return ResponseEntity.ok(Map.of("data", "This is not a public endpoint"));
    }
}
