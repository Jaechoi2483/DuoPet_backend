package com.petlogue.duopetbackend.admin.controller;


import com.petlogue.duopetbackend.admin.model.dto.DashboardDataDto;
import com.petlogue.duopetbackend.admin.model.service.AdminService;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
public class AdminController {
    private final AdminService adminService;


    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserDto>> getUserList(
            Pageable pageable,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "status", required = false) String status
    ) {
        // 이제 서비스 계층으로 필터 조건들을 전달합니다.
        Page<UserDto> userPage = adminService.findAllUsers(pageable, role, status);
        return ResponseEntity.ok(userPage);
    }
    @PatchMapping("/admin/users/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable("userId") Long userId,
            @RequestBody UserDto request) {

        adminService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable("userId") Long userId,
            @RequestBody UserDto request) {

        adminService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok().build();
    }



    // 대시보드 데이터 조회 엔드포인트 추가
    @GetMapping("/admin/dashboard")
    public ResponseEntity<DashboardDataDto> getDashboardData() {
        DashboardDataDto dashboardData = adminService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}
