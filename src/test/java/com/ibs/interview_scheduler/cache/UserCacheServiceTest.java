package com.ibs.interview_scheduler.cache;

import com.ibs.interview_scheduler.dtos.responseDto.UserResponseDTO;
import com.ibs.interview_scheduler.feign.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserCacheServiceTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserCacheService userCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------- getAllUsers --------------------
    @Test
    void getAllUsers_shouldReturnListFromFeignClient() {
        UserResponseDTO user1 = new UserResponseDTO(1, "john_doe", "9876543210", "secret",
                "john@example.com", "John Doe", true, 1, "ADMIN", null, null);
        UserResponseDTO user2 = new UserResponseDTO(2, "jane_doe", "9876543211", "secret",
                "jane@example.com", "Jane Doe", true, 2, "HR", null, null);

        when(userClient.getAllUsers()).thenReturn(List.of(user1, user2));

        List<UserResponseDTO> result = userCacheService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).contains(user1, user2);
        verify(userClient, times(1)).getAllUsers();
    }

    // -------------------- fallbackGetAllUsers --------------------
    @Test
    void fallbackGetAllUsers_shouldReturnEmptyList() {
        Throwable throwable = new RuntimeException("Service down");

        List<UserResponseDTO> result = userCacheService.fallbackGetAllUsers(throwable);

        assertThat(result).isEmpty();
    }

    // -------------------- getUserIdNameMap (when cache empty) --------------------
    @Test
    void getUserIdNameMap_shouldBuildCacheWhenEmpty() {
        UserResponseDTO user1 = new UserResponseDTO(1, "john_doe", "9876543210", "secret",
                "john@example.com", "John Doe", true, 1, "ADMIN", null, null);
        UserResponseDTO user2 = new UserResponseDTO(2, "jane_doe", "9876543211", "secret",
                "jane@example.com", "Jane Doe", true, 2, "HR", null, null);

        when(userClient.getAllUsers()).thenReturn(List.of(user1, user2));

        Map<Integer, String> result = userCacheService.getUserIdNameMap();

        assertThat(result)
                .hasSize(2)
                .containsEntry(1, "John Doe")
                .containsEntry(2, "Jane Doe");

        verify(userClient, times(1)).getAllUsers();
    }

    // -------------------- getUserIdNameMap (when cache already built) --------------------
    @Test
    void getUserIdNameMap_shouldReturnCachedMapIfNotEmpty() {
        // First call populates the cache
        UserResponseDTO user = new UserResponseDTO(1, "john", "123", "pwd",
                "john@mail.com", "John Doe", true, 1, "ADMIN", null, null);
        when(userClient.getAllUsers()).thenReturn(List.of(user));
        userCacheService.getUserIdNameMap();

        // Second call should not trigger userClient again
        Map<Integer, String> result = userCacheService.getUserIdNameMap();

        assertThat(result)
                .hasSize(1)
                .containsEntry(1, "John Doe");
        verify(userClient, times(1)).getAllUsers(); // still only once
    }

    // -------------------- refreshUserCache --------------------
    @Test
    void refreshUserCache_shouldUpdateCacheFromFeign() {
        UserResponseDTO user1 = new UserResponseDTO(1, "john", "123", "pwd",
                "john@mail.com", "John Doe", true, 1, "ADMIN", null, null);
        UserResponseDTO user2 = new UserResponseDTO(2, "jane", "456", "pwd",
                "jane@mail.com", "Jane Doe", true, 2, "HR", null, null);

        when(userClient.getAllUsers()).thenReturn(List.of(user1, user2));

        userCacheService.refreshUserCache();

        Map<Integer, String> cache = userCacheService.getUserIdNameMap();

        assertThat(cache)
                .hasSize(2)
                .containsEntry(1, "John Doe")
                .containsEntry(2, "Jane Doe");
        verify(userClient, atLeastOnce()).getAllUsers();
    }
}
