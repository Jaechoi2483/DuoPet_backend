package com.petlogue.duopetbackend.consultation.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConsultationDto {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotNull(message = "수의사 ID는 필수입니다")
    private Long vetId;
    
    private Long petId; // Optional
    
    private Long scheduleId; // Optional - for scheduled consultations
    
    private String consultationType; // CHAT, VIDEO, PHONE (default: CHAT)
    
    private LocalDateTime scheduledDatetime; // For immediate consultation, can be null
    
    private String chiefComplaint; // 주요 증상/상담 사유
    
    private PaymentInfoDto paymentInfo; // 결제 정보 (Optional)
}