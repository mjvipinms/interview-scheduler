package com.ibs.interview_scheduler.feign;

import com.ibs.interview_scheduler.dtos.responseDto.ReportResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "userservice", configuration = FeignClientsConfiguration.class)
public interface UserClient {

    @GetMapping("/api/v1/users/all")
    List<UserResponseDTO> getAllUsers();

    @GetMapping("/api/v1/users/report")
    ReportResponseDto getUserReport(
            @RequestParam String role,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir
    );
}
