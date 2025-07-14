package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.user.model.dto.UserDto;
import com.petlogue.duopetbackend.user.model.service.UserService;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 회원가입 1단계 - 아이디, 비밀번호 입력 처리
     */
    @PostMapping("/signup/step1")
    public ResponseEntity<String> signup(@RequestBody UserDto userDto) {
        userService.signup(userDto);
        return ResponseEntity.ok("회원가입 1단계 완료");
    }

    /**
     * 회원가입 2단계 - 개인정보 입력 + 프로필 이미지 업로드
     */
    @PostMapping("/signup/step2")
    public ResponseEntity<?> signupStep2(
            @RequestPart("data") UserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            UserDto processed = userService.processStep2(userDto, file);
            return ResponseEntity.ok(processed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 회원가입 4단계 - 최종 회원 등록 처리
     * - 프로필 이미지 포함
     * - 전문가/보호소일 경우 각 Service 호출
     */
    @PostMapping("/signup/final")
    public ResponseEntity<?> signupFinal(
            @RequestPart("data") UserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            userService.saveFinalUser(userDto, file);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("회원가입 실패: " + e.getMessage());
        }
    }

    /**
     * 아이디 중복 확인
     */
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId);
        return ResponseEntity.ok(exists); // true = 중복 O
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(exists); // true = 중복 O
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String userEmail) {
        boolean exists = userRepository.existsByUserEmail(userEmail);
        return ResponseEntity.ok(exists); // true = 중복 O
    }
}
