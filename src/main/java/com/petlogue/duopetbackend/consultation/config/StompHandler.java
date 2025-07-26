// /consultation/config/StompHandler.java
package com.petlogue.duopetbackend.consultation.config;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JWTUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP CONNECT 단계에서만 JWT 토큰 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            log.info("STOMP CONNECT - Authorization Header: {}", jwtToken);

            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                String token = jwtToken.substring(7);

                if (!jwtUtil.isTokenExpired(token)) {
                    String loginId = jwtUtil.getLoginIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);

                    // Spring Security 인증 토큰(Principal) 생성
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            loginId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))); // "ROLE_" 접두사 추가

                    // WebSocket 세션에 인증 정보 저장
                    accessor.setUser(authentication);
                    log.info("STOMP 인증 성공: {}, 역할: {}", loginId, role);
                }
            }
        }
        return message;
    }
}