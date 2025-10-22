package com.ibs.interview_scheduler.controller;

import com.ibs.interview_scheduler.dtos.responseDto.AdminDashboardResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.HrDashboardResponseDto;
import com.ibs.interview_scheduler.dtos.responseDto.PanelDashboardResponseDto;
import com.ibs.interview_scheduler.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/hr-summary")
    public ResponseEntity<HrDashboardResponseDto> getHrDashboardSummary(
            @RequestHeader(value = "X-User-Role") String role) {
        if (!"HR".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dashboardService.getHrDashboardSummary());
    }

    @GetMapping("/panel-summary/{panelId}")
    public ResponseEntity<PanelDashboardResponseDto> getPanelDashboard(@PathVariable Integer panelId,@RequestHeader(value = "X-User-Role") String role) {
        if (!"PANEL".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dashboardService.getPanelDashboard(panelId));
    }
    @GetMapping("/admin-summary")
    public ResponseEntity<AdminDashboardResponseDto> getAdminDashboardSummary(
            @RequestHeader(value = "X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dashboardService.getAdminDashboardSummary());
    }

}

