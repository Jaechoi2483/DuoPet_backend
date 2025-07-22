package com.petlogue.duopetbackend.health.model.dto;

import com.petlogue.duopetbackend.health.jpa.entity.HealthRecordEntity;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordDto {
    
    private Long recordId;
    private Long petId;
    private String petName; // 조회 시 반려동물 이름 포함
    private String recordType;
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate recordDate;
    private String hospitalName;
    private String veterinarian;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<HealthFileDto> files = new ArrayList<>();
    
    // Entity to DTO 변환
    public static HealthRecordDto fromEntity(HealthRecordEntity entity) {
        return HealthRecordDto.builder()
                .recordId(entity.getRecordId())
                .petId(entity.getPet().getPetId())
                .petName(entity.getPet().getPetName())
                .recordType(entity.getRecordType())
                .title(entity.getTitle())
                .recordDate(entity.getRecordDate())
                .hospitalName(entity.getHospitalName())
                .veterinarian(entity.getVeterinarian())
                .content(entity.getContent())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}