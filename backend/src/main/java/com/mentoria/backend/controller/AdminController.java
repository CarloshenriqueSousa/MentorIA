package com.mentoria.backend.controller;

import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.dto.response.admin.AdminOverviewDto;
import com.mentoria.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewDto>> overview() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getOverview()));
    }
}
