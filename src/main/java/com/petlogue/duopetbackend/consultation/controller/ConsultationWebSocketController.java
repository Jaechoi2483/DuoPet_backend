package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.consultation.model.dto.ChatMessageDto;
import com.petlogue.duopetbackend.consultation.model.dto.RoomStatusUpdateDto;
import com.petlogue.duopetbackend.consultation.model.dto.TypingIndicatorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ConsultationWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final com.petlogue.duopetbackend.consultation.model.service.ChatMessageService chatMessageService;
    private final com.petlogue.duopetbackend.user.jpa.repository.UserRepository userRepository;
    private final com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository consultationRoomRepository;
    
    /**
     * Handle incoming chat messages
     * Client sends to: /app/consultation/{roomUuid}/send
     * Server broadcasts to: /topic/consultation/{roomUuid}
     */
    @MessageMapping("/consultation/{roomUuid}/send")
    @SendTo("/topic/consultation/{roomUuid}")
    public ChatMessageDto handleChatMessage(
            @DestinationVariable String roomUuid,
            @Payload ChatMessageDto message,
            @Header("simpSessionId") String sessionId,
            Principal principal) {
        
        log.info("Received message in room {}: {} from user {}", 
                roomUuid, message.getContent(), principal.getName());
        
        try {
            // 사용자 정보 조회
            com.petlogue.duopetbackend.user.jpa.entity.UserEntity sender = 
                userRepository.findByLoginId(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));
            
            // 상담방 정보 조회
            com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom room = 
                consultationRoomRepository.findByRoomUuid(roomUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomUuid));
            
            // 메시지 DTO 완성
            message.setRoomId(room.getRoomId());
            message.setSenderId(sender.getUserId());
            message.setSenderUsername(principal.getName());
            message.setSenderName(sender.getNickname());
            message.setSenderRole(sender.getRole());
            message.setSentAt(LocalDateTime.now());
            message.setSessionId(sessionId);
            
            // 메시지 저장
            com.petlogue.duopetbackend.consultation.jpa.entity.ChatMessage savedMessage = 
                chatMessageService.saveMessage(message);
            
            // 저장된 메시지를 기반으로 완전한 DTO 생성
            ChatMessageDto responseDto = ChatMessageDto.builder()
                .messageId(savedMessage.getMessageId())
                .roomId(room.getRoomId())
                .roomUuid(roomUuid)
                .senderId(sender.getUserId())
                .senderUsername(sender.getLoginId())
                .senderRole(sender.getRole())
                .senderName(sender.getNickname())
                .content(message.getContent())
                .messageType(message.getMessageType() != null ? message.getMessageType() : "TEXT")
                .sentAt(savedMessage.getCreatedAt())
                .isRead(false)
                .build();
            
            log.info("Message saved and broadcasting: messageId={}, senderId={}, senderUsername={}, senderName={}", 
                responseDto.getMessageId(), responseDto.getSenderId(), responseDto.getSenderUsername(), responseDto.getSenderName());
            
            return responseDto;
            
        } catch (Exception e) {
            log.error("Error handling chat message: ", e);
            throw new RuntimeException("Failed to process message", e);
        }
    }
    
    /**
     * Handle typing indicator
     * Client sends to: /app/consultation/{roomUuid}/typing
     * Server broadcasts to: /topic/consultation/{roomUuid}/typing
     */
    @MessageMapping("/consultation/{roomUuid}/typing")
    @SendTo("/topic/consultation/{roomUuid}/typing")
    public TypingIndicatorDto handleTypingIndicator(
            @DestinationVariable String roomUuid,
            @Payload TypingIndicatorDto typing,
            Principal principal) {
        
        typing.setUsername(principal.getName());
        typing.setTimestamp(LocalDateTime.now());
        
        return typing;
    }
    
    /**
     * Handle room status updates (join, leave, etc.)
     * Client sends to: /app/consultation/{roomUuid}/status
     * Server broadcasts to: /topic/consultation/{roomUuid}/status
     */
    @MessageMapping("/consultation/{roomUuid}/status")
    @SendTo("/topic/consultation/{roomUuid}/status")
    public RoomStatusUpdateDto handleRoomStatus(
            @DestinationVariable String roomUuid,
            @Payload RoomStatusUpdateDto status,
            Principal principal) {
        
        log.info("Room {} status update: {} by user {}", 
                roomUuid, status.getStatus(), principal.getName());
        
        status.setUsername(principal.getName());
        status.setTimestamp(LocalDateTime.now());
        
        // TODO: Update room status in database via service
        
        return status;
    }
    
    /**
     * Handle errors for specific user
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        log.error("WebSocket error: ", exception);
        return exception.getMessage();
    }
    
    /**
     * Send notification to specific user
     * This method can be called from service layer
     */
    public void sendNotificationToUser(String username, String notification) {
        messagingTemplate.convertAndSendToUser(
                username, 
                "/queue/notifications", 
                notification
        );
    }
    
    /**
     * Broadcast message to all users in a room
     * This method can be called from service layer
     */
    public void broadcastToRoom(String roomUuid, Object message) {
        messagingTemplate.convertAndSend(
                "/topic/consultation/" + roomUuid, 
                message
        );
    }
}