package com.example.file_transfert.security.aspect;

import com.example.file_transfert.exception.InsufficientPermissionException;
import com.example.file_transfert.model.Permission;
import com.example.file_transfert.security.annotation.RequirePermission;
import com.example.file_transfert.security.annotation.IsSharedWithActiveUser;
import com.example.file_transfert.service.interfaces.IAccessControlService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.aspectj.lang.reflect.MethodSignature;

@Aspect
@Component
public class PermissionAspect {
    private final IAccessControlService accessControlService;

    public PermissionAspect(IAccessControlService accessControlService) {
        this.accessControlService = accessControlService;
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

    @Around("@annotation(isSharedWithActiveUser)")
    public Object checkIfFileIsSharedWithConnectedUser(ProceedingJoinPoint joinPoint, IsSharedWithActiveUser isSharedWithActiveUser) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        String username = authentication.getName();

        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String names[] = sig.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String filename = null;
        for (int i = 0; i < filename.length(); i++) {
            if (names[i].equals(isSharedWithActiveUser.value())) {
                filename = (String) args[i];
                break;
            }
        }
        if (filename == null) throw new IllegalArgumentException("Missing Argument");

        if (!accessControlService.isFileSharedWith(username, filename, isSharedWithActiveUser.permission())) {
            throw new AccessDeniedException("You cannot access this file");
        }
        return joinPoint.proceed();
    }
}
