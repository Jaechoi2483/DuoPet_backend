package com.petlogue.duopetbackend.consultation.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    // Track active sessions and their subscriptions
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
        
        sessionUserMap.put(sessionId, username);
        log.info("WebSocket Connected - Session: {}, User: {}", sessionId, username);
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = sessionUserMap.get(sessionId);
        String roomUuid = sessionRoomMap.get(sessionId);
        
        if (roomUuid != null) {
            // Notify other users in the room that this user has left
            Map<String, Object> notification = Map.of(
                "type", "USER_LEFT",
                "username", username != null ? username : "anonymous",
                "message", "User has left the consultation"
            );
            
            messagingTemplate.convertAndSend(
                "/topic/consultation/" + roomUuid + "/status",
                notification
            );
            
            sessionRoomMap.remove(sessionId);
        }
        
        sessionUserMap.remove(sessionId);
        log.info("WebSocket Disconnected - Session: {}, User: {}", sessionId, username);
    }
    
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        
        // Extract room UUID from destination if it's a consultation room subscription
        if (destination != null && destination.startsWith("/topic/consultation/")) {
            String roomUuid = extractRoomUuid(destination);
            if (roomUuid != null) {
                sessionRoomMap.put(sessionId, roomUuid);
                String username = sessionUserMap.get(sessionId);
                
                // Notify other users in the room that this user has joined
                Map<String, Object> notification = Map.of(
                    "type", "USER_JOINED",
                    "username", username != null ? username : "anonymous",
                    "message", "User has joined the consultation"
                );
                
                messagingTemplate.convertAndSend(
                    "/topic/consultation/" + roomUuid + "/status",
                    notification
                );
                
                log.info("User {} subscribed to room {}", username, roomUuid);
            }
        }
    }
    
    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = sessionUserMap.get(sessionId);
        
        log.info("Session {} (User: {}) unsubscribed", sessionId, username);
    }
    
    private String extractRoomUuid(String destination) {
        // Extract room UUID from destination like "/topic/consultation/{roomUuid}"
        String[] parts = destination.split("/");
        if (parts.length >= 4 && "consultation".equals(parts[2])) {
            return parts[3];
        }
        return null;
    }
    
    public boolean isUserOnline(String username) {
        return sessionUserMap.containsValue(username);
    }
    
    public int getActiveUsersInRoom(String roomUuid) {
        return (int) sessionRoomMap.values().stream()
                .filter(room -> room.equals(roomUuid))
                .count();
    }
}