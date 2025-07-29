package com.petlogue.duopetbackend.security.handler;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.security.jwt.model.service.RefreshService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshService refreshService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("소셜 로그인 성공 핸들러 진입");

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String loginId = null;
        String providerId = null;

        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        log.info("소셜 제공자: {}", registrationId);

        switch (registrationId) {
            case "kakao":
                providerId = oauth2User.getAttributes().get("id").toString();
                loginId = "kakao_" + providerId;
                break;

            case "naver":
                Map<String, Object> responseMap = (Map<String, Object>) oauth2User.getAttributes().get("response");
                providerId = responseMap.get("id").toString();
                loginId = "naver_" + providerId;
                break;

            case "google":
                providerId = oauth2User.getAttributes().get("sub").toString(); // 구글은 "sub" 키
                loginId = "google_" + providerId;
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        }

        log.info("loginId: {}, providerId: {}", loginId, providerId);

        final String finalLoginId = loginId;
        // 사용자 조회
        UserEntity user = userRepository.findByLoginId(finalLoginId)
                .orElseThrow(() -> new RuntimeException("소셜 로그인 사용자 없음: " + finalLoginId));

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(user.toDto(), "access");
        String refreshToken = jwtUtil.generateToken(user.toDto(), "refresh");

        // Refresh 토큰 저장
        refreshService.saveOrUpdate(user.getUserId(), refreshToken,
                request.getRemoteAddr(), request.getHeader("User-Agent"));

        boolean isNew = "social_temp".equalsIgnoreCase(user.getStatus());

        String redirectUrl = String.format("http://localhost:3000/social-redirect?accessToken=%s&refreshToken=%s&isNew=%s&provider=%s",
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8),
                isNew,
                registrationId);

        if (!response.isCommitted()) {
            response.sendRedirect(redirectUrl);
        }
    }
}
