package com.petlogue.duopetbackend.admin.controller;


import com.petlogue.duopetbackend.admin.model.dto.DashboardDataDto;
import com.petlogue.duopetbackend.admin.model.service.AdminService;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;

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

    @GetMapping("/admin/users/{userId}")
    public ResponseEntity<UserDto> getUserDetail(@PathVariable Long userId) {
        UserDto userDetail = adminService.findUserDetailById(userId);
        return ResponseEntity.ok(userDetail);
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
    @PostMapping("/admin/chatbot/resync")
    public ResponseEntity<String> resyncChatbotData() {
        log.info("관리자: 챗봇 데이터 동기화 API 호출됨");

        // To-Do: AdminService를 호출하여 실제 동기화 로직을 수행해야 합니다.
         adminService.resyncChatbotData();

        return ResponseEntity.ok("챗봇 데이터 동기화 요청이 정상적으로 접수되었습니다.");
    }


    // 대시보드 데이터 조회 엔드포인트 추가
    @GetMapping("/admin/dashboard")
    public ResponseEntity<DashboardDataDto> getDashboardData() {
        DashboardDataDto dashboardData = adminService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/admin/files/vet/{filename}")
    public ResponseEntity<Resource> getVetFile(@PathVariable String filename) throws IOException {
        Resource resource = adminService.loadVetFile(filename);
        String contentType = determineContentType(resource);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/admin/files/shelter/{filename}")
    public ResponseEntity<Resource> getShelterFile(@PathVariable String filename) throws IOException {
        Resource resource = adminService.loadShelterFile(filename);
        String contentType = determineContentType(resource);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private String determineContentType(Resource resource) throws IOException {
        String contentType = Files.probeContentType(resource.getFile().toPath());
        if (contentType == null) {
            contentType = "application/octet-stream"; // 타입을 알 수 없을 때의 기본값
        }
        return contentType;
    }
}
