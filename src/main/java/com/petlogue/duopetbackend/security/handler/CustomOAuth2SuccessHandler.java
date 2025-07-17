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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

        // KakaoService에서 처리된 OAuth2User 정보를 가져옵니다.
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // KakaoService에서 생성한 loginId 형식("kakao_" + id)으로 사용자를 조회합니다.
        // KakaoUserDto의 id 필드는 String 타입으로 변환되어 저장되었으므로 getAttribute로 가져옵니다.
        String providerId = oauth2User.getAttributes().get("id").toString();
        String loginId = "kakao_" + providerId;

        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("소셜 로그인 처리 중 사용자를 찾을 수 없습니다: " + loginId));

        // JWT 발급
        String accessToken = jwtUtil.generateToken(user.toDto(), "access");
        String refreshToken = jwtUtil.generateToken(user.toDto(), "refresh");

        // 리프레시 토큰을 DB에 저장 또는 업데이트
        refreshService.saveOrUpdate(user.getUserId(), refreshToken, request.getRemoteAddr(), request.getHeader("User-Agent"));

        // KakaoService에서 저장한 상태('social_temp')를 기준으로 신규 사용자 여부를 정확히 판단합니다.
        boolean isNew = "social_temp".equals(user.getStatus());

        log.info("소셜 로그인 성공 → 토큰 발급 후 리디렉션: isNew={}, loginId={}", isNew, loginId);

        // 리디렉션 URL 구성
        String redirectUrl = String.format("http://localhost:3000/social-redirect?accessToken=%s&refreshToken=%s&isNew=%s",
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8),
                isNew);

        if (!response.isCommitted()) {
            response.sendRedirect(redirectUrl);
        }
    }
}
