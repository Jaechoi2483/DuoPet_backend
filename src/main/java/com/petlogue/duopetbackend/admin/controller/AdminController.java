package com.petlogue.duopetbackend.admin.controller;


import com.petlogue.duopetbackend.admin.model.service.AdminService;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
public class AdminController {
    private final AdminService adminService;


    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserDto>> getUserList(Pageable pageable) {
        Page<UserDto> userPage = adminService.findAllUsers(pageable);
        return ResponseEntity.ok(userPage);
    }
}
