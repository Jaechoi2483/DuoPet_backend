package com.petlogue.duopetbackend.consultation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // Allow anyone to connect to the WebSocket
                .simpDestMatchers("/ws-consultation/**").permitAll()
                // Require authentication for subscription destinations
                .simpSubscribeDestMatchers("/user/**", "/topic/**", "/queue/**").authenticated()
                // Require authentication for sending messages
                .simpDestMatchers("/app/**").authenticated()
                // Deny all other messages
                .anyMessage().denyAll();
    }
    
    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket to allow cross-origin requests
        return true;
    }
}