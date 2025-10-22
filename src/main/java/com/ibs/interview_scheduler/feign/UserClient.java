package com.ibs.interview_scheduler.feign;

import com.ibs.interview_scheduler.dtos.responseDto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "userservice", configuration = FeignClientsConfiguration.class)
public interface UserClient {

    @GetMapping("/api/v1/users/all")
    List<UserResponseDTO> getAllUsers();
}
