// /consultation/config/WebSocketAuthInterceptor.java (전체 교체)
package com.petlogue.duopetbackend.consultation.config;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private final JWTUtil jwtUtil;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        // STOMP 프로토콜의 CONNECT 명령어일 때만 인증 처리
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("WebSocket CONNECT 요청 감지. 인증을 시작합니다.");
            
            // 헤더에서 'Authorization' 값을 찾아 JWT 토큰을 추출합니다.
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("헤더에서 JWT 토큰을 발견했습니다.");
                
                try {
                    // 토큰 유효성 검증
                    if (!jwtUtil.isTokenExpired(token)) {
                        String username = jwtUtil.getLoginIdFromToken(token);
                        String role = jwtUtil.getRoleFromToken(token);
                        
                        log.info("WebSocket 인증 성공 - username: {}, role: {}", username, role);
                        
                        // Spring Security가 이해할 수 있는 인증 객체를 생성합니다.
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        );
                        
                        // accessor에 인증된 사용자 정보를 저장합니다.
                        // 이 작업 덕분에 convertAndSendToUser가 동작할 수 있습니다.
                        accessor.setUser(auth);
                    } else {
                        log.warn("만료된 WebSocket 토큰입니다.");
                    }
                } catch (Exception e) {
                    log.error("WebSocket 토큰 검증 중 오류 발생: ", e);
                }
            } else {
                log.warn("WebSocket 연결에 유효한 Authorization 헤더가 없습니다.");
            }
        }
        
        return message;
    }
}