package com.petlogue.duopetbackend.health.jpa.entity;

import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PET_HEALTH_RECORD")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pet_health_record_seq")
    @SequenceGenerator(name = "pet_health_record_seq", sequenceName = "PET_HEALTH_RECORD_SEQ", allocationSize = 1)
    @Comment("건강기록 ID")
    private Long recordId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @Comment("반려동물 ID")
    private PetEntity pet;
    
    @Column(nullable = false, length = 50)
    @Comment("기록 유형 (정기검진, 예방접종, 치료, 응급처치, 기타)")
    private String recordType;
    
    @Column(nullable = false, length = 200)
    @Comment("제목")
    private String title;
    
    @Column(nullable = false)
    @Comment("기록 날짜")
    private LocalDate recordDate;
    
    @Column(length = 100)
    @Comment("병원명")
    private String hospitalName;
    
    @Column(length = 100)
    @Comment("담당 수의사")
    private String veterinarian;
    
    @Column(columnDefinition = "TEXT")
    @Comment("상세 내용")
    private String content;
    
    @Column(length = 20)
    @Comment("상태 (완료, 예정)")
    @Builder.Default
    private String status = "완료";
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    @Comment("수정일시")
    private LocalDateTime updatedAt;
    
    // 파일 첨부는 별도 테이블로 관리
}