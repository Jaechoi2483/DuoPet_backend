package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRoomDto {
    
    private Long roomId;
    private String roomUuid;
    
    // User info
    private Long userId;
    private String userName;
    
    // Vet info
    private Long vetId;
    private String vetName;
    private String clinicName;
    
    // Pet info
    private Long petId;
    private String petName;
    
    // Room status
    private String roomStatus;
    private String consultationType;
    
    // Time info
    private LocalDateTime scheduledDatetime;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    
    // Payment info
    private BigDecimal consultationFee;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paidAt;
    
    // Consultation content
    private String chiefComplaint;
    private String consultationNotes;
    private String prescription;
    
    // Meta info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional info
    private Integer unreadMessageCount;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    
    // Review info
    private boolean hasReview;
}