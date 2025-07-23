package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    
    private Long messageId;
    private Long roomId;
    private String roomUuid;
    private Long senderId;
    private String senderUsername;
    private String senderRole; // USER or VET
    private String content;
    private String messageType; // TEXT, IMAGE, FILE, SYSTEM
    private String fileUrl;
    private String fileName;
    private LocalDateTime sentAt;
    private boolean isRead;
    private boolean isImportant;
    private String sessionId;
    
    // For real-time display
    private String senderName;
    private String senderProfileImage;
}