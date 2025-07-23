package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.model.dto.ChatMessageDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.ChatMessage;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.jpa.repository.ChatMessageRepository;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ConsultationRoomRepository consultationRoomRepository;
    private final UserRepository userRepository;
    
    /**
     * 채팅 메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(ChatMessageDto dto) {
        // Validate consultation room
        ConsultationRoom room = consultationRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        // Validate sender
        UserEntity sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        
        // Create and save message
        ChatMessage message = ChatMessage.builder()
                .consultationRoom(room)
                .sender(sender)
                .content(dto.getContent())
                .messageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT")
                .fileUrl(dto.getFileUrl())
                .fileName(dto.getFileName())
                .isRead("N")
                .isImportant(dto.isImportant() ? "Y" : "N")
                .build();
        
        // Add message to room
        room.addMessage(message);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Message saved: room={}, sender={}, type={}", 
                room.getRoomId(), sender.getUserId(), message.getMessageType());
        
        return savedMessage;
    }
    
    /**
     * 상담방의 메시지 목록 조회 (페이징)
     */
    public Page<ChatMessageDto> getRoomMessages(Long roomId, Pageable pageable) {
        Page<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId, pageable);
        
        return messages.map(this::toDto);
    }
    
    /**
     * 상담방의 최근 메시지 조회
     */
    public List<ChatMessageDto> getRecentMessages(Long roomId, int limit) {
        List<ChatMessage> messages = chatMessageRepository
                .findRecentMessages(roomId, PageRequest.of(0, limit));
        
        return messages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 읽지 않은 메시지 수 조회
     */
    public Long getUnreadMessageCount(Long roomId, Long userId) {
        return chatMessageRepository.countUnreadMessages(roomId, userId);
    }
    
    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessagesAsRead(Long roomId, Long readerId) {
        int updatedCount = chatMessageRepository.markMessagesAsRead(
                roomId, readerId, LocalDateTime.now());
        
        log.info("Marked {} messages as read in room {} by user {}", 
                updatedCount, roomId, readerId);
    }
    
    /**
     * 시스템 메시지 생성
     */
    @Transactional
    public void createSystemMessage(Long roomId, String content) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        // System user (ID = 1 or create a dedicated system user)
        UserEntity systemUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("System user not found"));
        
        ChatMessage message = ChatMessage.builder()
                .consultationRoom(room)
                .sender(systemUser)
                .content(content)
                .messageType("SYSTEM")
                .isRead("Y")
                .isImportant("N")
                .build();
        
        chatMessageRepository.save(message);
    }
    
    /**
     * 중요 메시지 표시/해제
     */
    @Transactional
    public void toggleImportantMessage(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        message.setIsImportant("Y".equals(message.getIsImportant()) ? "N" : "Y");
        chatMessageRepository.save(message);
    }
    
    /**
     * 메시지 검색
     */
    public List<ChatMessageDto> searchMessages(Long roomId, String keyword) {
        List<ChatMessage> messages = chatMessageRepository.searchMessages(roomId, keyword);
        
        return messages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 첨부파일이 있는 메시지 조회
     */
    public List<ChatMessageDto> getMessagesWithAttachments(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findMessagesWithAttachments(roomId);
        
        return messages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 마지막 메시지 조회
     */
    public ChatMessageDto getLastMessage(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository
                .findLastMessage(roomId, PageRequest.of(0, 1));
        
        return messages.isEmpty() ? null : toDto(messages.get(0));
    }
    
    /**
     * ChatMessage를 DTO로 변환
     */
    private ChatMessageDto toDto(ChatMessage message) {
        UserEntity sender = message.getSender();
        String senderRole = "USER";
        
        // Determine sender role based on the role field
        if (sender.getRole() != null) {
            if ("VET".equals(sender.getRole())) {
                senderRole = "VET";
            } else if ("ADMIN".equals(sender.getRole())) {
                senderRole = "SYSTEM";
            }
        }
        
        return ChatMessageDto.builder()
                .messageId(message.getMessageId())
                .roomUuid(message.getConsultationRoom().getRoomUuid())
                .senderId(sender.getUserId())
                .senderUsername(sender.getUserEmail())
                .senderRole(senderRole)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .sentAt(message.getCreatedAt())
                .isRead("Y".equals(message.getIsRead()))
                .isImportant("Y".equals(message.getIsImportant()))
                .senderName(sender.getNickname())
                .senderProfileImage(sender.getRenameFilename())
                .build();
    }
}