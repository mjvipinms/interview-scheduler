package com.ibs.interview_scheduler.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class RoleValidator {

    private RoleValidator() {}

    public static boolean isAuthorized(String role, List<String> requiredRoles) {
        return requiredRoles.contains(role);
    }

    public static <T> ResponseEntity<T> unauthorizedResponse(T responseBody) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
    }
}
