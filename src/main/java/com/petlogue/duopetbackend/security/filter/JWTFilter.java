package com.petlogue.duopetbackend.security.filter;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
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
                || url.equals("/users/check-id")
                || url.equals("/users/check-nickname")
                || url.equals("/users/check-email")
                || url.startsWith("/users/signup")
                || url.equals("/social-redirect")
                || url.equals("/vet/upload-temp")
                || url.equals("/vet/register")
                || url.equals("/shelter/upload-temp")
                || url.equals("/shelter/register")
                || url.equals("/shelter/check")
                || url.equals("/pet/register")
                || url.equals("/sms/send")
                || url.equals("/sms/verify")
                || url.equals("/users/find-id")
                || url.equals("/users/check-user")
                || url.equals("/users/reset-password")
                || url.equals("/users/face-upload")
                || url.equals("/users/check-face")
                || url.equals("/api/v1/face-login/verify")
                || url.equals("/face-login/success")

                || url.equals("/reissue")


                || url.startsWith("/api/info")
                || url.startsWith("/info")
                || url.startsWith("/api/adoption")
                || url.startsWith("/api/hospitals")

                || url.startsWith("/board/view-count")
                || url.startsWith("/board/freeList")
                || url.startsWith("/board/detail")
                || url.equals("/board/top-liked")
                || url.equals("/board/top-viewed")
                || url.startsWith("/comments/view")
                || url.equals("/notice")
                || url.startsWith("/upload/")
                || url.startsWith("/pet/list/")
                || url.startsWith("/pet/image/")
                || url.matches("/pet/\\d+")
                
                // 전문가 상담 공개 API 추가
                || url.equals("/api/consultation/vet-profiles/available")
                || url.equals("/api/consultation/vet-profiles/online")
                || url.equals("/api/consultation/vet-profiles/search")
                || url.equals("/api/consultation/vet-profiles/top-rated")
                || url.equals("/api/consultation/vet-profiles/test/all")
                || url.matches("/api/consultation/vet-profiles/vet/\\d+")
                || url.matches("/api/consultation/consultation-reviews/vet/\\d+")
                || url.matches("/api/consultation/vet-schedules/vet/\\d+/available");

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        log.info("JWTFilter 실행: {}", requestURI);

        if (requestURI.startsWith("/admin/board")) {
            log.info("★★★ /admin/board 요청 감지: {}", requestURI);
        }
        // Debug logging
        if (requestURI.startsWith("/api/adoption")) {
            log.info("Adoption API 요청 감지: {}", requestURI);
            log.info("isExcludedUrl 결과: {}", isExcludedUrl(requestURI));
        }

        if (HttpMethod.GET.matches(requestMethod) && requestURI.equals("/faq")) {
            log.info("인증 예외 경로 (GET /faq): 토큰 검사 없이 통과 → {} {}", requestMethod, requestURI);
            filterChain.doFilter(request, response);
            return; // 중요: 다음 필터로 넘긴 후 바로 리턴
        }

        if (HttpMethod.GET.matches(requestMethod) && requestURI.startsWith("/notice")) {
            log.info("인증 예외 경로 (GET /notice/**): 토큰 검사 없이 통과 → {} {}", requestMethod, requestURI);
            filterChain.doFilter(request, response);
            return; // 토큰 검사 로직을 건너뛰고 다음 필터로 진행
        }

        if (isExcludedUrl(requestURI)) {
            log.info("인증 예외 경로: 토큰 검사 없이 통과 → {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        else {
            log.info("→ 인증 필요 경로로 판단됨: {}", requestURI);
        }

        String accessTokenHeader = request.getHeader("Authorization");
        String refreshTokenHeader = request.getHeader("RefreshToken");
        
        log.info("받은 헤더 - Authorization: {}", accessTokenHeader != null ? "있음" : "없음");
        log.info("받은 헤더 - RefreshToken: {}", refreshTokenHeader != null ? "있음" : "없음");

        if (accessTokenHeader == null || accessTokenHeader.isEmpty() ||
                refreshTokenHeader == null || refreshTokenHeader.isEmpty()) {
            log.warn("토큰 누락 - Authorization: {}, RefreshToken: {}", 
                    accessTokenHeader != null ? "있음" : "없음",
                    refreshTokenHeader != null ? "있음" : "없음");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"missing or invalid tokens\"}");
            return;
        }

        try {
            String accessToken = accessTokenHeader.replace("Bearer ", "");
            String refreshToken = refreshTokenHeader.replace("Bearer ", "");

            log.info("추출된 Access Token (Bearer 제외): {}", accessToken.substring(0, Math.min(accessToken.length(), 20)) + "..."); // 앞 20자만 출력
            log.info("추출된 Refresh Token (Bearer 제외): {}", refreshToken.substring(0, Math.min(refreshToken.length(), 20)) + "..."); // 앞 20자만 출력
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


                Claims claims = jwtUtil.getClaimsFromToken(accessToken);

                Object userNoObj = claims.get("userNo");

                if (userNoObj == null) {
                    log.error("JWTFilter - userNo가 null입니다.");
                    throw new IllegalArgumentException("userNo가 존재하지 않습니다.");
                }

                Long userId;
                if (userNoObj instanceof Integer) {
                    userId = ((Integer) userNoObj).longValue();
                } else if (userNoObj instanceof Long) {
                    userId = (Long) userNoObj;
                } else if (userNoObj instanceof String) {
                    userId = Long.valueOf((String) userNoObj);
                } else {
                    throw new IllegalArgumentException("userNo 형식이 잘못되었습니다. 타입: " + userNoObj.getClass());
                }

                log.info("JWTFilter - 추출된 userId: {}", userId);
                request.setAttribute("userId", userId);

                String loginId = jwtUtil.getLoginIdFromToken(accessToken);
                String role = jwtUtil.getRoleFromToken(accessToken);
                log.info("JWTFilter - 로그인 ID: {}", loginId);
                log.info("JWTFilter - 권한: {}", role);

                request.setAttribute("loginId", loginId);

            // 2. 인증 객체 생성
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(loginId, null,
                            java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(role)));

            // 3. SecurityContext에 인증 정보 등록
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);

            // 정상 토큰 → 필터 통과
            filterChain.doFilter(request, response);



        } catch (Exception e) {
            log.error("JWTFilter 토큰 검사 중 예외 발생", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"internal server error\"}");
        }
    }
}
