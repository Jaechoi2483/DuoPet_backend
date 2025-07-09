package com.petlogue.duopetbackend.security.filter;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 특정 URL은 토큰 검사를 제외 (permitAll이더라도 필터는 거치기 때문에 추가 체크 필요)
    private boolean isExcludedUrl(String url) {
        return url.equals("/")
                || url.equals("/favicon.ico")
                || url.equals("/login")
                || url.equals("/auth/signup")
                || url.equals("/auth/id-check")
                || url.equals("/auth/email-check")
                || url.equals("/auth/reissue")
                || url.equals("/faq")
                || url.startsWith("/qna")
                || url.startsWith("/notice")
                || url.startsWith("/board")
                || url.endsWith(".png");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("JWTFilter 실행: {}", requestURI);

        if (isExcludedUrl(requestURI)) {
            log.info("인증 예외 경로: 토큰 검사 없이 통과 → {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String accessTokenHeader = request.getHeader("Authorization");
        String refreshTokenHeader = request.getHeader("RefreshToken");

        if (accessTokenHeader == null || accessTokenHeader.isEmpty() ||
                refreshTokenHeader == null || refreshTokenHeader.isEmpty()) {
            log.warn("토큰 누락 - Authorization or RefreshToken 헤더 없음");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"missing or invalid tokens\"}");
            return;
        }

        try {
            String accessToken = accessTokenHeader.replace("Bearer ", "");
            String refreshToken = refreshTokenHeader.replace("Bearer ", "");

            if (!jwtUtil.isTokenExpired(accessToken) && jwtUtil.isTokenExpired(refreshToken)) {
                log.warn("⚠RefreshToken 만료, AccessToken만 유효");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("token-expired", "RefreshToken");
                response.getWriter().write("{\"error\":\"RefreshToken expired\"}");
                return;
            }

            if (jwtUtil.isTokenExpired(accessToken)) {
                log.warn("⚠AccessToken 만료, RefreshToken은 유효");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("token-expired", "AccessToken");
                response.getWriter().write("{\"error\":\"AccessToken expired\"}");
                return;
            }

            // 정상 토큰 → 필터 통과
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWTFilter 토큰 검사 중 예외 발생", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"internal server error\"}");
        }
    }
}
