package com.petlogue.duopetbackend.consultation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // 모든 WebSocket 메시지 허용 (개발 중 임시)
                .nullDestMatcher().permitAll()
                .simpDestMatchers("/**").permitAll()
                .simpSubscribeDestMatchers("/**").permitAll()
                .simpMessageDestMatchers("/**").permitAll()
                .anyMessage().permitAll();
    }
    
    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket to allow cross-origin requests
        return true;
    }
}