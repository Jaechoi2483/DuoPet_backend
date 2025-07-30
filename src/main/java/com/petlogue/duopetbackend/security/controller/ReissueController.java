package com.petlogue.duopetbackend.security.controller;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.security.jwt.model.service.RefreshService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshService refreshService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("[ReissueController] 토큰 재발급 요청");

        String accessTokenHeader = request.getHeader("Authorization");
        String refreshTokenHeader = request.getHeader("RefreshToken");
        String extendLogin = request.getHeader("ExtendLogin");

        try {
            String accessToken = (accessTokenHeader != null && accessTokenHeader.startsWith("Bearer "))
                    ? accessTokenHeader.substring(7).trim() : null;

            String refreshToken = (refreshTokenHeader != null && refreshTokenHeader.startsWith("Bearer "))
                    ? refreshTokenHeader.substring(7).trim() : null;

            if (accessToken == null || refreshToken == null) {
                log.warn("AccessToken 또는 RefreshToken 누락");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid tokens");
            }

            boolean isAccessTokenExpired = jwtUtil.isTokenExpired(accessToken);
            boolean isRefreshTokenExpired = jwtUtil.isTokenExpired(refreshToken);

            log.info("AccessToken 만료 여부: {}", isAccessTokenExpired);
            log.info("RefreshToken 만료 여부: {}", isRefreshTokenExpired);

            // 사용자 정보 추출용 (모든 경우에 대비)
            Long userId;
            try {
                userId = jwtUtil.getUserIdFromToken(isAccessTokenExpired ? refreshToken : accessToken);
            } catch (Exception e) {
                log.error("토큰에서 사용자 ID 추출 실패", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            // IP + 디바이스 정보 추출
            String ipAddress = request.getRemoteAddr();
            String deviceInfo = request.getHeader("User-Agent");

            // 1. 토큰 유효 + 연장 요청일 때 → accessToken 재발급
            if (!isAccessTokenExpired && !isRefreshTokenExpired) {
                if ("true".equalsIgnoreCase(extendLogin)) {
                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("사용자 없음: " + userId));

                    String newAccessToken = jwtUtil.generateToken(user.toDto(), "access");
                    response.setHeader("Authorization", "Bearer " + newAccessToken);
                    response.setHeader("Refresh-Token", "Bearer " + refreshToken);
                    response.setHeader("Access-Control-Expose-Headers", "Authorization, Refresh-Token");

                    log.info("(강제연장) 새 accessToken 발급됨: {}", newAccessToken);
                    log.info("(강제연장) 새 accessToken 만료 시각: {}", jwtUtil.getExpiration(newAccessToken));
                    return ResponseEntity.ok("Access token reissued by user request");
                }

                return ResponseEntity.ok("Tokens still valid");
            }

            // 2. 둘 다 만료 → 리프레시 토큰 삭제
            if (isAccessTokenExpired && isRefreshTokenExpired) {
                Long tokenId = refreshService.findTokenId(userId, refreshToken);
                if (tokenId != null) {
                    refreshService.deleteRefresh(tokenId);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login session expired");
            }

            // 3. access 만료 + refresh 유효 → accessToken 재발급
            if (isAccessTokenExpired && !isRefreshTokenExpired) {
                UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("사용자 없음: " + userId));

                String newAccessToken = jwtUtil.generateToken(user.toDto(), "access");
                response.setHeader("Authorization", "Bearer " + newAccessToken);
                response.setHeader("Refresh-Token", "Bearer " + refreshToken);
                response.setHeader("Access-Control-Expose-Headers", "Authorization, Refresh-Token");

                log.info("새 accessToken 발급됨: {}", newAccessToken);
                log.info("새 accessToken 만료 시각: {}", jwtUtil.getExpiration(newAccessToken));

                // 여기서 refreshToken 갱신 기록도 함께 수행!
                refreshService.saveOrUpdate(userId, refreshToken, ipAddress, deviceInfo);

                return ResponseEntity.ok("Access token reissued");
            }

            return ResponseEntity.ok("Tokens still valid");

        } catch (Exception e) {
            log.error("토큰 재발급 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reissue token");
        }
    }

}
