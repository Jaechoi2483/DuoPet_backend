package com.petlogue.duopetbackend.consultation.jpa.entity;

import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vet_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"vet"})
public class VetProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false, unique = true)
    private VetEntity vet;
    
    @Column(name = "introduction", columnDefinition = "CLOB")
    @Lob
    private String introduction;
    
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal consultationFee = new BigDecimal("30000");
    
    @Column(name = "is_available", length = 1, nullable = false)
    @Builder.Default
    private String isAvailable = "Y";
    
    @Column(name = "is_online", length = 1, nullable = false)
    @Builder.Default
    private String isOnline = "N";
    
    @Column(name = "last_online_at")
    private LocalDateTime lastOnlineAt;
    
    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;
    
    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;
    
    @Column(name = "consultation_count")
    @Builder.Default
    private Integer consultationCount = 0;
    
    @Column(name = "response_time_avg")
    @Builder.Default
    private Integer responseTimeAvg = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 편의 메서드
    public boolean isAvailable() {
        return "Y".equals(this.isAvailable);
    }
    
    public boolean isOnline() {
        return "Y".equals(this.isOnline);
    }
    
    public void setOnlineStatus(boolean online) {
        this.isOnline = online ? "Y" : "N";
        if (online) {
            this.lastOnlineAt = LocalDateTime.now();
        }
    }
    
    public void updateRating(BigDecimal newRating) {
        if (this.ratingCount == 0) {
            this.ratingAvg = newRating;
            this.ratingCount = 1;
        } else {
            BigDecimal totalRating = this.ratingAvg.multiply(new BigDecimal(this.ratingCount));
            totalRating = totalRating.add(newRating);
            this.ratingCount++;
            this.ratingAvg = totalRating.divide(new BigDecimal(this.ratingCount), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    public void incrementConsultationCount() {
        this.consultationCount++;
    }
}