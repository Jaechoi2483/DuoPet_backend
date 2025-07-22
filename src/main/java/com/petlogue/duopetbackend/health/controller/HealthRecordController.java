package com.petlogue.duopetbackend.health.controller;

import com.petlogue.duopetbackend.health.model.dto.HealthRecordDto;
import com.petlogue.duopetbackend.health.model.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class HealthRecordController {
    
    private final HealthRecordService healthRecordService;
    
    /**
     * 건강 기록 생성
     */
    @PostMapping(value = "/records", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createHealthRecord(
            @RequestPart("data") HealthRecordDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            log.info("건강 기록 생성 요청 - petId: {}, title: {}", dto.getPetId(), dto.getTitle());
            HealthRecordDto savedRecord = healthRecordService.createHealthRecord(dto, files);
            log.info("건강 기록 생성 성공 - recordId: {}", savedRecord.getRecordId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("건강 기록 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    
    /**
     * 건강 기록 조회 (단건)
     */
    @GetMapping("/records/{recordId}")
    public ResponseEntity<?> getHealthRecord(@PathVariable Long recordId) {
        try {
            HealthRecordDto record = healthRecordService.getHealthRecord(recordId);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            log.error("건강 기록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
    
    /**
     * 반려동물별 건강 기록 목록 조회
     */
    @GetMapping("/records/pet/{petId}")
    public ResponseEntity<?> getHealthRecordsByPet(@PathVariable Long petId) {
        try {
            log.info("건강 기록 목록 조회 요청 - petId: {}", petId);
            List<HealthRecordDto> records = healthRecordService.getHealthRecordsByPet(petId);
            log.info("건강 기록 목록 조회 성공 - 건수: {}", records.size());
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("건강 기록 목록 조회 중 오류 발생 - petId: {}", petId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    
    /**
     * 건강 기록 수정
     */
    @PutMapping("/records/{recordId}")
    public ResponseEntity<?> updateHealthRecord(
            @PathVariable Long recordId,
            @RequestBody HealthRecordDto dto
    ) {
        try {
            HealthRecordDto updatedRecord = healthRecordService.updateHealthRecord(recordId, dto);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            log.error("건강 기록 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    
    /**
     * 건강 기록 삭제
     */
    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<?> deleteHealthRecord(@PathVariable Long recordId) {
        try {
            healthRecordService.deleteHealthRecord(recordId);
            return ResponseEntity.ok("건강 기록이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("건강 기록 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}