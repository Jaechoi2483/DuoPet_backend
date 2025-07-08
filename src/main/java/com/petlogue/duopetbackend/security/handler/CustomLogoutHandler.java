package com.petlogue.duopetbackend.security.handler;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.security.jwt.model.service.RefreshService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;
    private final UserRepository userRepository;  // 추가됨

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("[CustomLogoutHandler] 로그아웃 요청 시작");

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String accessToken = authorization.substring("Bearer ".length()).trim();

            try {
                // 1️ accessToken에서 loginId 추출
                String loginId = jwtUtil.getLoginIdFromToken(accessToken);

                // 2️ loginId → userId(PK) 조회
                UserEntity user = userRepository.findByLoginId(loginId)
                        .orElseThrow(() -> new RuntimeException("해당 loginId에 대한 회원 없음"));

                Long userId = user.getUserId();

                // 3️ userId → refreshToken 조회
                String refreshToken = refreshService.findTokenByUserId(userId);

                // 4️ userId + token 으로 토큰 ID 조회
                Long tokenId = refreshService.findTokenId(userId, refreshToken);

                // 5️ 해당 tokenId 삭제
                refreshService.deleteRefresh(tokenId);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("로그아웃 성공");
                log.info("로그아웃 성공");
            } catch (Exception e) {
                log.error("로그아웃 중 예외 발생", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try {
                    response.getWriter().write("로그아웃 처리 중 오류 발생");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        } else {
            log.warn("Authorization 헤더 누락 또는 Bearer 형식 아님");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write("유효하지 않은 요청");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
