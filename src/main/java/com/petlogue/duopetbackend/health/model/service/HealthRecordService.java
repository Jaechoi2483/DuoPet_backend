package com.petlogue.duopetbackend.health.model.service;

import com.petlogue.duopetbackend.health.jpa.entity.HealthFileEntity;
import com.petlogue.duopetbackend.health.jpa.entity.HealthRecordEntity;
import com.petlogue.duopetbackend.health.jpa.repository.HealthFileRepository;
import com.petlogue.duopetbackend.health.jpa.repository.HealthRecordRepository;
import com.petlogue.duopetbackend.health.model.dto.HealthFileDto;
import com.petlogue.duopetbackend.health.model.dto.HealthRecordDto;
import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HealthRecordService {
    
    private final HealthRecordRepository healthRecordRepository;
    private final HealthFileRepository healthFileRepository;
    private final PetRepository petRepository;
    
    private static final String UPLOAD_DIR = "C:/upload_files/health/";
    
    /**
     * 건강 기록 생성
     */
    public HealthRecordDto createHealthRecord(HealthRecordDto dto, List<MultipartFile> files) throws IOException {
        // 반려동물 확인
        PetEntity pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다."));
        
        // 건강 기록 엔티티 생성
        HealthRecordEntity record = HealthRecordEntity.builder()
                .pet(pet)
                .recordType(dto.getRecordType())
                .title(dto.getTitle())
                .recordDate(dto.getRecordDate())
                .hospitalName(dto.getHospitalName())
                .veterinarian(dto.getVeterinarian())
                .content(dto.getContent())
                .status(dto.getStatus() != null ? dto.getStatus() : "완료")
                .build();
        
        HealthRecordEntity savedRecord = healthRecordRepository.save(record);
        
        // 파일 업로드 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                saveHealthFile(savedRecord, file);
            }
        }
        
        // DTO 반환
        HealthRecordDto resultDto = HealthRecordDto.fromEntity(savedRecord);
        resultDto.setFiles(getHealthFiles(savedRecord));
        
        return resultDto;
    }
    
    /**
     * 건강 기록 조회 (단건)
     */
    @Transactional(readOnly = true)
    public HealthRecordDto getHealthRecord(Long recordId) {
        HealthRecordEntity record = healthRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("건강 기록을 찾을 수 없습니다."));
        
        HealthRecordDto dto = HealthRecordDto.fromEntity(record);
        dto.setFiles(getHealthFiles(record));
        
        return dto;
    }
    
    /**
     * 반려동물별 건강 기록 목록 조회
     */
    @Transactional(readOnly = true)
    public List<HealthRecordDto> getHealthRecordsByPet(Long petId) {
        PetEntity pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다."));
        
        List<HealthRecordEntity> records = healthRecordRepository.findByPetOrderByRecordDateDesc(pet);
        
        return records.stream()
                .map(record -> {
                    HealthRecordDto dto = HealthRecordDto.fromEntity(record);
                    dto.setFiles(getHealthFiles(record));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 건강 기록 수정
     */
    public HealthRecordDto updateHealthRecord(Long recordId, HealthRecordDto dto) {
        HealthRecordEntity record = healthRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("건강 기록을 찾을 수 없습니다."));
        
        // 수정 가능한 필드 업데이트
        record.setRecordType(dto.getRecordType());
        record.setTitle(dto.getTitle());
        record.setRecordDate(dto.getRecordDate());
        record.setHospitalName(dto.getHospitalName());
        record.setVeterinarian(dto.getVeterinarian());
        record.setContent(dto.getContent());
        record.setStatus(dto.getStatus());
        
        HealthRecordEntity savedRecord = healthRecordRepository.save(record);
        
        HealthRecordDto resultDto = HealthRecordDto.fromEntity(savedRecord);
        resultDto.setFiles(getHealthFiles(savedRecord));
        
        return resultDto;
    }
    
    /**
     * 건강 기록 삭제
     */
    public void deleteHealthRecord(Long recordId) {
        HealthRecordEntity record = healthRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("건강 기록을 찾을 수 없습니다."));
        
        // 첨부 파일 삭제
        List<HealthFileEntity> files = healthFileRepository.findByHealthRecord(record);
        for (HealthFileEntity file : files) {
            deleteFile(file.getFilePath());
        }
        healthFileRepository.deleteByHealthRecord(record);
        
        // 건강 기록 삭제
        healthRecordRepository.delete(record);
    }
    
    /**
     * 파일 저장
     */
    private void saveHealthFile(HealthRecordEntity record, MultipartFile file) throws IOException {
        // 디렉토리 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        String filePath = UPLOAD_DIR + storedFilename;
        
        // 파일 저장
        file.transferTo(new File(filePath));
        
        // DB 저장
        HealthFileEntity fileEntity = HealthFileEntity.builder()
                .healthRecord(record)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .filePath(filePath)
                .build();
        
        healthFileRepository.save(fileEntity);
    }
    
    /**
     * 건강 기록의 파일 목록 조회
     */
    private List<HealthFileDto> getHealthFiles(HealthRecordEntity record) {
        List<HealthFileEntity> files = healthFileRepository.findByHealthRecord(record);
        return files.stream()
                .map(HealthFileDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 파일 삭제
     */
    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("파일 삭제 실패: " + filePath, e);
        }
    }
}