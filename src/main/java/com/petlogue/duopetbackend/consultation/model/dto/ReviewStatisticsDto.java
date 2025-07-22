package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatisticsDto {
    
    private Double averageRating;
    private Double averageKindnessScore;
    private Double averageProfessionalScore;
    private Double averageResponseScore;
    private Integer totalReviews;
    
    // Rating distribution
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
    
    // Calculate percentage for each rating
    public Double getFiveStarPercentage() {
        return totalReviews > 0 ? (fiveStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getFourStarPercentage() {
        return totalReviews > 0 ? (fourStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getThreeStarPercentage() {
        return totalReviews > 0 ? (threeStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getTwoStarPercentage() {
        return totalReviews > 0 ? (twoStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getOneStarPercentage() {
        return totalReviews > 0 ? (oneStarCount * 100.0) / totalReviews : 0.0;
    }
}