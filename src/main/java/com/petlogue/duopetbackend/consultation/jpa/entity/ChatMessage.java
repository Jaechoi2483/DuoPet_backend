package com.petlogue.duopetbackend.consultation.jpa.entity;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"consultationRoom", "sender"})
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ConsultationRoom consultationRoom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;
    
    @Column(name = "sender_type", length = 10, nullable = false)
    private String senderType; // USER, VET, SYSTEM
    
    @Column(name = "message_type", length = 20, nullable = false)
    @Builder.Default
    private String messageType = "TEXT";
    
    @Column(name = "content", columnDefinition = "CLOB")
    @Lob
    private String content;
    
    @Column(name = "file_url", length = 500)
    private String fileUrl;
    
    @Column(name = "file_name", length = 255)
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    @Column(name = "is_read", length = 1, nullable = false)
    @Builder.Default
    private String isRead = "N";
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "is_important", length = 1, nullable = false)
    @Builder.Default
    private String isImportant = "N";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 메시지 타입 상수
    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM, NOTICE
    }
    
    public enum SenderType {
        USER, VET, SYSTEM
    }
    
    // 편의 메서드
    public boolean isRead() {
        return "Y".equals(this.isRead);
    }
    
    public void markAsRead() {
        this.isRead = "Y";
        this.readAt = LocalDateTime.now();
    }
    
    public boolean isImportant() {
        return "Y".equals(this.isImportant);
    }
    
    public void markAsImportant() {
        this.isImportant = "Y";
    }
    
    public boolean isSystemMessage() {
        return SenderType.SYSTEM.name().equals(this.senderType);
    }
    
    public boolean hasAttachment() {
        return this.fileUrl != null && !this.fileUrl.isEmpty();
    }
    
    // Builder 패턴에서 senderType 자동 설정
    public static class ChatMessageBuilder {
        public ChatMessageBuilder sender(UserEntity sender) {
            this.sender = sender;
            // UserEntity의 role에 따라 senderType 자동 설정
            if (sender != null && sender.getRole() != null) {
                this.senderType = "vet".equals(sender.getRole()) ? 
                    SenderType.VET.name() : SenderType.USER.name();
            }
            return this;
        }
    }
}