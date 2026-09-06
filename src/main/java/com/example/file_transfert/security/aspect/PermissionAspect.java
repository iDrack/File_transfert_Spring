package com.example.file_transfert.security.aspect;

import com.example.file_transfert.exception.InsufficientPermissionException;
import com.example.file_transfert.model.Permission;
import com.example.file_transfert.security.annotation.IsOwner;
import com.example.file_transfert.security.annotation.RequirePermission;
import com.example.file_transfert.security.annotation.IsSharedWithActiveUser;
import com.example.file_transfert.service.UserService;
import com.example.file_transfert.service.interfaces.IAccessControlService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class PermissionAspect {
    private final IAccessControlService accessControlService;
    private final UserService userService;

    public PermissionAspect(IAccessControlService accessControlService, UserService userService) {
        this.accessControlService = accessControlService;
        this.userService = userService;
    }

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        String username = authentication.getName();
        Permission permission = requirePermission.value();
        boolean allowed = accessControlService.hasPermission(username, permission);
        if (!allowed) {
            throw new InsufficientPermissionException(permission);
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(isOwner)")
    public Object checkIfActiveUserIsOwner(ProceedingJoinPoint joinPoint, IsOwner isOwner) throws Throwable {
        PermissionContext ctx = resolvePermissionContext(joinPoint);

        if (ctx.filename == null) throw new IllegalArgumentException("Missing Argument");
        accessControlService.checkUserOwnership(ctx.username, ctx.filename);
        return joinPoint.proceed();
    }

    @Around("@annotation(isSharedWithActiveUser)")
    public Object checkIfFileIsSharedWithConnectedUser(ProceedingJoinPoint joinPoint, IsSharedWithActiveUser isSharedWithActiveUser) throws Throwable {
        PermissionContext ctx = resolvePermissionContext(joinPoint);
        if (ctx.filename == null) throw new IllegalArgumentException("Missing Argument");

        if (!accessControlService.isFileSharedWith(ctx.username, ctx.filename, isSharedWithActiveUser.permission())) {
            throw new AccessDeniedException("You cannot access this file");
        }
        return joinPoint.proceed();
    }

    private PermissionContext resolvePermissionContext(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Object[] args = joinPoint.getArgs();
        String filename = Arrays.stream(args)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Filename not found in method arguments"));

        return new PermissionContext(username, filename);
    }

    private record PermissionContext(String username, String filename) {}
}
