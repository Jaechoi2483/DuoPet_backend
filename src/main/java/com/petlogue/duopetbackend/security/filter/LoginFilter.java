package com.petlogue.duopetbackend.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.security.jwt.jpa.entity.RefreshToken;
import com.petlogue.duopetbackend.security.jwt.model.service.RefreshService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshService refreshService;

    public LoginFilter(AuthenticationManager authenticationManager,
                       JWTUtil jwtUtil,
                       UserRepository userRepository,
                       RefreshService refreshService) {
        this.setAuthenticationManager(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshService = refreshService;
        this.setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String loginId = null;
        String userPwd = null;

        try {
            // 요청 바디 내용을 문자열로 먼저 읽어 출력해보기
            String rawBody = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines()
                    .reduce("", (acc, cur) -> acc + cur);

            log.info("🟡 LoginFilter: 요청 바디(raw): {}", rawBody);

            if (rawBody == null || rawBody.isBlank()) {
                throw new RuntimeException("요청 바디가 비어 있습니다.");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> requestBody = mapper.readValue(rawBody, Map.class);
            loginId = requestBody.get("loginId");
            userPwd = requestBody.get("userPwd");

            // 클라이언트에서 전달한 로그인 정보 로그 출력
            log.info("클라이언트에서 전달한 loginId: {}", loginId);
            log.info("클라이언트에서 전달한 비밀번호: {}", userPwd);

        } catch (IOException e) {
            throw new RuntimeException("요청 데이터를 읽을 수 없습니다.", e);
        }

        if (loginId == null || userPwd == null) {
            throw new RuntimeException("아이디 또는 비밀번호가 전달되지 않았습니다.");
        }

        return this.getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(loginId, userPwd));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain, Authentication authentication)
            throws IOException, ServletException {

        String loginId = authentication.getName();

        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("로그인 사용자를 찾을 수 없습니다: " + loginId));

        if ("suspended".equals(user.getStatus())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"로그인 정지된 계정입니다. 관리자에게 문의하세요.\"}");
            return;
        }

        // 사용자 정보 로그 추가
        log.info("로그인 성공 - userId: {}, loginId: {}, nickname: {}", 
                user.getUserId(), user.getLoginId(), user.getNickname());
        
        // 1. 토큰 생성
        String accessToken = jwtUtil.generateToken(user.toDto(), "access");
        String refreshToken = jwtUtil.generateToken(user.toDto(), "refresh");

        // 2. RefreshToken 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getUserId())
                .refreshToken(refreshToken)
                .ipAddress(request.getRemoteAddr())  // 클라이언트 IP
                .deviceInfo(request.getHeader("User-Agent"))  // 브라우저/디바이스 정보
                .createdAt(LocalDateTime.now())
                .expiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
                .tokenStatus("ACTIVE")
                .build();

        refreshService.saveRefresh(refreshTokenEntity);

        // 3. 응답
        Map<String, Object> responseBody = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "userId", user.getUserId(),
                "nickname", user.getNickname(),
                "role", user.getRole()
        );

        response.setContentType("application/json; charset=utf-8");
        new ObjectMapper().writeValue(response.getWriter(), responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=utf-8");

        String errorMessage;
        if (failed.getMessage().contains("Bad credentials")) {
            errorMessage = "아이디와 비밀번호를 다시 확인해 주세요.";
        } else {
            errorMessage = "로그인 실패: 알 수 없는 오류가 발생했습니다.";
        }

        response.getWriter().write(String.format("{\"error\":\"%s\"}", errorMessage));
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return request.getMethod().equals("POST") && request.getRequestURI().equals("/login");
    }

}
