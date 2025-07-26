// /consultation/config/WebSocketConfig.java
package com.petlogue.duopetbackend.consultation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the messages
        // back to the client on destinations prefixed with "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set the prefix for messages bound for methods annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Set the prefix for user destinations
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws-consultation" endpoint
        // This is the endpoint that the clients will use to connect to our WebSocket server
        registry.addEndpoint("/ws-consultation")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001") // React app origins
                .addInterceptors(new WebSocketHandshakeInterceptor()) // URL 파라미터 처리
                .withSockJS(); // Enable SockJS fallback options
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인증 인터셉터 추가
        registration.interceptors(webSocketAuthInterceptor);
    }
}