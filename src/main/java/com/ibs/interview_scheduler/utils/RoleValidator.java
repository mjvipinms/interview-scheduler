package com.ibs.interview_scheduler.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RoleValidator {

    private RoleValidator() {}

    public static boolean isAuthorized(String role, String requiredRole) {
        return role != null && role.equalsIgnoreCase(requiredRole);
    }

    public static <T> ResponseEntity<T> unauthorizedResponse(T responseBody) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
    }
}
