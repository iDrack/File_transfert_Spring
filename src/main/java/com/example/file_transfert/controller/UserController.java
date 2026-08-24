package com.example.file_transfert.controller;

import com.example.file_transfert.dto.user.UserLogin;
import com.example.file_transfert.dto.user.UserRead;
import com.example.file_transfert.dto.user.UserUpdate;
import com.example.file_transfert.model.Permission;
import com.example.file_transfert.model.User;
import com.example.file_transfert.security.annotation.RequirePermission;
import com.example.file_transfert.service.interfaces.IUserService;
import com.example.file_transfert.utils.DtoTools;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private DtoTools dtoTools;
    @Value("${MODE:dev}")
    private String mode;

    /**
     * Get the curent user profile.
     *
     * @param currentUser User logged in.
     * @return User profile.
     */
    @GetMapping(value = "/profile", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserRead> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dtoTools.convertToDto(currentUser, UserRead.class));
    }

    /**
     * Fetch every user's information.
     *
     * @return List of every user with their public information.
     */
    @GetMapping(value = "/", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @RequirePermission(Permission.USER_READ)
    public ResponseEntity<List<UserRead>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers()
                .stream()
                .map(user ->
                        dtoTools.convertToDto(user, UserRead.class))
                .toList());
    }

    /**
     * Update the current user with new infos, require the "USER_UPDATE_SELF" permission (intended to be used by a user).
     *
     * @param user       User currently logged in and loaded in the SpringSecurity context.
     * @param userUpdate New user infos.
     * @return Use with updated infos.
     */
    @PutMapping(value = "/", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @RequirePermission(Permission.USER_UPDATE_SELF)
    public ResponseEntity<Map<String, String>> updateUserProfile(@AuthenticationPrincipal User user, @RequestBody UserUpdate userUpdate) {
        try {
            if (userService.testCredentials(user, userUpdate.getPassword())) {
                User updatedUser = userService.update(user, userUpdate);
                return ResponseEntity.ok(Map.of("data", updatedUser.getUsername() + " has been updated successfully."));
            } else {
                throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "Password is incorrect.");
            }
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Disable the current user account.
     *
     * @param user      User loaded in the Spring Security context.
     * @param userLogin Needed to validate the user's password.
     * @return Message on successful deactivation.
     */
    @DeleteMapping(value = "/")
    @PreAuthorize("isAuthenticated()")
    @RequirePermission(Permission.USER_DELETE_SELF)
    public ResponseEntity<Map<String, String>> disableUser(@AuthenticationPrincipal User user, @RequestBody UserLogin userLogin, HttpServletResponse response) {
        try {
            if (userService.testCredentials(user, userLogin.getPassword())) {
                userService.disableAccount(user.getUsername());
                //Log out user
                ResponseCookie clear = ResponseCookie.from("refresh_token")
                        .httpOnly(true)
                        .secure(mode.equals("production"))
                        .sameSite("Lax")
                        .path("/api/auth")
                        .maxAge(0)
                        .build();
                response.setHeader(HttpHeaders.SET_COOKIE, clear.toString());

                return ResponseEntity.ok(Map.of(
                        "data", "Your account has been disabled."
                ));
            } else {
                throw new HttpClientErrorException(HttpStatusCode.valueOf(403), "Password is incorrect.");
            }
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Delete a user permanently with the submitted username, required the "USER_DELETE" permission (intended to be used by admin).
     *
     * @param username The user to delete.
     * @return Message with the use who has  been deleted.
     */
    @DeleteMapping(value = "/{username}")
    @PreAuthorize("isAuthenticated()")
    @RequirePermission(Permission.USER_DELETE)
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        if (userService.deleteByUsername(username)) {
            return ResponseEntity.ok(Map.of(
                    "data", "User " + username + " has been deleted permanently."
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "User not found."
            ));
        }
    }
}
