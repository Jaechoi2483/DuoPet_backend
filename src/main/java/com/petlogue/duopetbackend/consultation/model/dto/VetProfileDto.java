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
public class VetProfileDto {
    
    private Long profileId;
    private Long vetId;
    private String vetName;
    private String clinicName;
    private String introduction;
    private BigDecimal consultationFee;
    private String isAvailable;
    private String isOnline;
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalConsultations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional vet info
    private String vetEmail;
    private String vetPhone;
    private String vetAddress;
    private String vetProfileImage;
}