package com.ibs.interview_scheduler.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleValidatorTest {

    // ---------------- isAuthorized ----------------

    @Test
    void isAuthorized_shouldReturnTrueWhenRoleIsInList() {
        boolean result = RoleValidator.isAuthorized("ADMIN", List.of("ADMIN", "HR", "PANEL"));
        assertThat(result).isTrue();
    }

    @Test
    void isAuthorized_shouldReturnFalseWhenRoleIsNotInList() {
        boolean result = RoleValidator.isAuthorized("CANDIDATE", List.of("ADMIN", "HR"));
        assertThat(result).isFalse();
    }

    @Test
    void isAuthorized_shouldReturnFalseWhenListIsEmpty() {
        boolean result = RoleValidator.isAuthorized("ADMIN", List.of());
        assertThat(result).isFalse();
    }

    @Test
    void isAuthorized_shouldBeCaseSensitive() {
        boolean result = RoleValidator.isAuthorized("admin", List.of("ADMIN"));
        assertThat(result).isFalse(); // not matching case
    }

    // ---------------- unauthorizedResponse ----------------

    @Test
    void unauthorizedResponse_shouldReturnForbiddenStatusWithBody() {
        String message = "Access Denied";
        ResponseEntity<String> response = RoleValidator.unauthorizedResponse(message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Access Denied");
    }

    @Test
    void unauthorizedResponse_shouldHandleNullBody() {
        ResponseEntity<String> response = RoleValidator.unauthorizedResponse(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNull();
    }
}
