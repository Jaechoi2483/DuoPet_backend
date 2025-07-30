package com.petlogue.duopetbackend.consultation.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class ConsultationReviewDto {
    
    private Long reviewId;
    private Long consultationRoomId;
    
    private Long userId;
    private String userName;
    
    private Long vetId;
    private String vetName;
    
    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다")
    private Integer rating;
    
    @Min(value = 1, message = "친절도 점수는 1점 이상이어야 합니다")
    @Max(value = 5, message = "친절도 점수는 5점 이하여야 합니다")
    private Integer kindnessScore;
    
    @Min(value = 1, message = "전문성 점수는 1점 이상이어야 합니다")
    @Max(value = 5, message = "전문성 점수는 5점 이하여야 합니다")
    private Integer professionalScore;
    
    @Min(value = 1, message = "응답속도 점수는 1점 이상이어야 합니다")
    @Max(value = 5, message = "응답속도 점수는 5점 이하여야 합니다")
    private Integer responseScore;
    
    private String reviewContent;
    private String vetReply;
    private boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}