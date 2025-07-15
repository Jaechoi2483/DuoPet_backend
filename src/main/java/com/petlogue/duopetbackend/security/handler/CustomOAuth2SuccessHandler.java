package com.petlogue.duopetbackend.security.handler;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("✅✅✅ 소셜 로그인 성공 핸들러 진입 ✅✅✅");

        String providerId = authentication.getName(); // 여기서는 프로바이더 ID (예: 카카오 ID)를 사용
        UserEntity user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> {
                    // 유저가 없으면 새로 생성
                    UserEntity newUser = UserEntity.builder()
                            .loginId(providerId)  // 프로바이더 ID로 로그인 ID 설정
                            .userName("소셜 로그인 사용자")  // 임시값으로 설정
                            .nickname("소셜 로그인 사용자")  // 임시값으로 설정
                            .userEmail("default@example.com")  // 임시값으로 설정
                            .phone("소셜가입")
                            .role("user")
                            .status("ACTIVE")
                            .createdAt(LocalDateTime.now())
                            .provider("KAKAO") // 프로바이더 설정
                            .providerId(providerId) // 실제 카카오 ID 저장
                            .build();
                    return userRepository.save(newUser);
                });

        // JWT 발급
        String accessToken = jwtUtil.generateToken(user.toDto(), "access");
        String refreshToken = jwtUtil.generateToken(user.toDto(), "refresh");

        // 최초 사용자 여부 확인 (status가 SOCIAL_TEMP일 경우만 true)
        boolean isNew = "social_temp".equals(user.getStatus());

        log.info("소셜 로그인 성공 → 토큰 발급 후 리디렉션: isNew={}, providerId={}", isNew, providerId);

        // 리디렉션 URL 구성
        String redirectUrl = String.format("http://localhost:3000/social-redirect?accessToken=%s&refreshToken=%s&isNew=%s",
                URLEncoder.encode(accessToken, "UTF-8"),
                URLEncoder.encode(refreshToken, "UTF-8"),
                isNew);
        log.info("리디렉션 URL: {}", redirectUrl);

        // 프론트엔드로 리디렉트
        response.sendRedirect(redirectUrl);
    }
}
