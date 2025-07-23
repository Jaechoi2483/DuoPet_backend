package com.petlogue.duopetbackend.consultation.jpa.entity;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"consultationRoom", "user", "vet"})
public class ConsultationReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, unique = true)
    private ConsultationRoom consultationRoom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private VetEntity vet;
    
    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5
    
    @Column(name = "kindness_score")
    private Integer kindnessScore; // 1-5
    
    @Column(name = "professional_score")
    private Integer professionalScore; // 1-5
    
    @Column(name = "response_score")
    private Integer responseScore; // 1-5
    
    @Column(name = "review_content", columnDefinition = "CLOB")
    @Lob
    private String reviewContent;
    
    @Column(name = "vet_reply", columnDefinition = "CLOB")
    @Lob
    private String vetReply;
    
    @Column(name = "is_visible", length = 1, nullable = false)
    @Builder.Default
    private String isVisible = "Y";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;
    
    // 편의 메서드
    public boolean isVisible() {
        return "Y".equals(this.isVisible);
    }
    
    public void hide() {
        this.isVisible = "N";
    }
    
    public void show() {
        this.isVisible = "Y";
    }
    
    public boolean hasVetReply() {
        return this.vetReply != null && !this.vetReply.isEmpty();
    }
    
    public void addVetReply(String reply) {
        this.vetReply = reply;
        this.repliedAt = LocalDateTime.now();
    }
    
    public Double getAverageScore() {
        int count = 0;
        int sum = 0;
        
        if (this.kindnessScore != null) {
            sum += this.kindnessScore;
            count++;
        }
        if (this.professionalScore != null) {
            sum += this.professionalScore;
            count++;
        }
        if (this.responseScore != null) {
            sum += this.responseScore;
            count++;
        }
        
        return count > 0 ? (double) sum / count : null;
    }
    
    // 유효성 검증
    @PrePersist
    @PreUpdate
    private void validateScores() {
        validateScore("rating", this.rating, true);
        validateScore("kindnessScore", this.kindnessScore, false);
        validateScore("professionalScore", this.professionalScore, false);
        validateScore("responseScore", this.responseScore, false);
    }
    
    private void validateScore(String fieldName, Integer score, boolean required) {
        if (required && score == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (score != null && (score < 1 || score > 5)) {
            throw new IllegalArgumentException(fieldName + " must be between 1 and 5");
        }
    }
}