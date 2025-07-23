package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.security.jwt.jpa.entity.RefreshToken;
import com.petlogue.duopetbackend.security.jwt.model.service.RefreshService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/face-login")
public class FaceLoginController {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;

    @PostMapping("/success")
    public ResponseEntity<?> faceLoginSuccess(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        log.info("🟢 얼굴 로그인 성공! 유저ID: {}, 닉네임: {}", userId, user.getNickname());

        // 토큰 생성
        String accessToken = jwtUtil.generateToken(user.toDto(), "access");
        String refreshToken = jwtUtil.generateToken(user.toDto(), "refresh");

        // RefreshToken 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .ipAddress("face-login") // 얼굴 로그인은 IP 없음
                .deviceInfo("face-login-device")
                .createdAt(LocalDateTime.now())
                .expiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
                .tokenStatus("ACTIVE")
                .build();

        refreshService.saveRefresh(refreshTokenEntity);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "userId", user.getUserId(),
                "nickname", user.getNickname(),
                "role", user.getRole()
        ));
    }
}
