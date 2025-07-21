package com.petlogue.duopetbackend.health.model.dto;

import com.petlogue.duopetbackend.health.jpa.entity.HealthFileEntity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthFileDto {
    
    private Long fileId;
    private Long recordId;
    private String originalFilename;
    private String storedFilename;
    private Long fileSize;
    private String contentType;
    private String downloadUrl; // 파일 다운로드 URL
    
    // Entity to DTO 변환
    public static HealthFileDto fromEntity(HealthFileEntity entity) {
        return HealthFileDto.builder()
                .fileId(entity.getFileId())
                .recordId(entity.getHealthRecord().getRecordId())
                .originalFilename(entity.getOriginalFilename())
                .storedFilename(entity.getStoredFilename())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .downloadUrl("/api/health/files/" + entity.getFileId()) // 다운로드 URL 생성
                .build();
    }
}