package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.user.model.dto.UserDto;
import com.petlogue.duopetbackend.user.model.service.UserService;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users") // ← /api 제거
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 회원가입 1단계 처리
     */
    @PostMapping("/signup/step1")
    public ResponseEntity<String> signup(@RequestBody UserDto userDto) {
        userService.signup(userDto);
        return ResponseEntity.ok("회원가입 1단계 완료");
    }

    /**
     * 아이디 중복 확인
     */
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId);
        return ResponseEntity.ok(exists); // true = 중복 O
    }
}
