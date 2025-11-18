package com.ibs.interview_scheduler.cache;

import com.ibs.interview_scheduler.dtos.responseDto.UserResponseDTO;
import com.ibs.interview_scheduler.feign.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

    private final UserClient userClient;
    private Map<Integer, String> userIdNameMap = new ConcurrentHashMap<>();

    @Cacheable("usersCache")
    @CircuitBreaker(name = "userServiceCircuitBreaker", fallbackMethod = "fallbackGetAllUsers")
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching users from UserService via Feign...");
        return userClient.getAllUsers();
    }

    /**
     * this is the fallback method for user service
     * @param t throwable object
     * @return empty list
     */
    public List<UserResponseDTO> fallbackGetAllUsers(Throwable t) {
        log.error("User Service unavailable. Returning fallback response. Error: {}", t.getMessage());
        return Collections.emptyList();
    }
    /**
     *
     * @return Map<Integer, String> with user id as key and  full name as value
     */
    public Map<Integer, String> getUserIdNameMap() {
        if (userIdNameMap.isEmpty()) {
            log.info("User map cache is empty, building from getAllUsers()...");
            updateUserMapCache(userClient.getAllUsers());
        }
        return userIdNameMap;
    }

    private void updateUserMapCache(List<UserResponseDTO> users) {
        userIdNameMap = users.stream()
                .collect(Collectors.toConcurrentMap(UserResponseDTO::getUserId, UserResponseDTO::getFullName));
    }

    /**
     * Periodically refresh cache (optional)
     */
    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void refreshUserCache() {
        log.info("Refreshing users cache from UserService...");
        updateUserMapCache(userClient.getAllUsers());
    }
}
