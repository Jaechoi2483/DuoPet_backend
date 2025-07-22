package com.petlogue.duopetbackend.health.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PET_HEALTH_FILE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthFileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pet_health_file_seq")
    @SequenceGenerator(name = "pet_health_file_seq", sequenceName = "PET_HEALTH_FILE_SEQ", allocationSize = 1)
    @Comment("파일 ID")
    private Long fileId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    @Comment("건강기록 ID")
    private HealthRecordEntity healthRecord;
    
    @Column(nullable = false, length = 255)
    @Comment("원본 파일명")
    private String originalFilename;
    
    @Column(nullable = false, length = 255)
    @Comment("저장된 파일명")
    private String storedFilename;
    
    @Column(nullable = false)
    @Comment("파일 크기")
    private Long fileSize;
    
    @Column(nullable = false, length = 100)
    @Comment("파일 타입")
    private String contentType;
    
    @Column(nullable = false, length = 500)
    @Comment("파일 저장 경로")
    private String filePath;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Comment("생성일시")
    private LocalDateTime createdAt;
}