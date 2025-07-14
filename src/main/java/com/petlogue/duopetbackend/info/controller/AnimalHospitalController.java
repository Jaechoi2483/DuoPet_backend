package com.petlogue.duopetbackend.info.controller;

import com.petlogue.duopetbackend.info.model.dto.AnimalHospitalDto;
import com.petlogue.duopetbackend.info.model.service.AnimalHospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class AnimalHospitalController {
    
    private final AnimalHospitalService hospitalService;
    
    /**
     * CSV 파일 임포트 (관리자 전용)
     */
    @PostMapping("/import/csv")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "파일이 비어있습니다"
                ));
            }
            
            // 파일 확장자 검사
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "CSV 파일만 업로드 가능합니다"
                ));
            }
            
            Map<String, Object> result = hospitalService.importFromCsv(file);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("CSV import error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "파일 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 영업 중인 병원 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<AnimalHospitalDto>> getHospitals(
            @PageableDefault(size = 20, sort = "businessName", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<AnimalHospitalDto> hospitals = hospitalService.getOperatingHospitals(pageable);
        return ResponseEntity.ok(hospitals);
    }
    
    /**
     * 키워드 검색
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AnimalHospitalDto>> searchHospitals(
            @RequestParam("keyword") String keyword,
            @PageableDefault(size = 20, sort = "businessName", direction = Sort.Direction.ASC) Pageable pageable) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Page.empty());
        }
        
        Page<AnimalHospitalDto> hospitals = hospitalService.searchByKeyword(keyword.trim(), pageable);
        return ResponseEntity.ok(hospitals);
    }
    
    /**
     * 시도별 병원 조회
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<Page<AnimalHospitalDto>> getHospitalsByCity(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "businessName", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<AnimalHospitalDto> hospitals = hospitalService.getHospitalsByCity(city, pageable);
        return ResponseEntity.ok(hospitals);
    }
    
    /**
     * 시군구별 병원 조회
     */
    @GetMapping("/city/{city}/district/{district}")
    public ResponseEntity<Page<AnimalHospitalDto>> getHospitalsByCityAndDistrict(
            @PathVariable String city,
            @PathVariable String district,
            @PageableDefault(size = 20, sort = "businessName", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<AnimalHospitalDto> hospitals = hospitalService.getHospitalsByCityAndDistrict(city, district, pageable);
        return ResponseEntity.ok(hospitals);
    }
    
    /**
     * 근처 병원 찾기
     */
    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyHospitals(
            @RequestParam("lat") Double latitude,
            @RequestParam("lon") Double longitude,
            @RequestParam(value = "radius", defaultValue = "5") Double radius) {
        
        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "위도와 경도는 필수입니다"
            ));
        }
        
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "올바른 좌표값이 아닙니다"
            ));
        }
        
        List<AnimalHospitalDto> hospitals = hospitalService.getNearbyHospitals(latitude, longitude, radius);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", hospitals.size());
        response.put("radius", radius);
        response.put("hospitals", hospitals);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 통계 정보 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = hospitalService.getStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 좌표 변환 테스트 (개발용)
     */
    @GetMapping("/test/coordinate")
    public ResponseEntity<Map<String, Object>> testCoordinateConversion(
            @RequestParam("x") Double x,
            @RequestParam("y") Double y) {
        
        // 이 메서드는 개발/테스트용입니다
        Map<String, Object> result = new HashMap<>();
        result.put("input", Map.of("x", x, "y", y));
        result.put("epsg", "EPSG:5174");
        
        // 예시 변환 결과
        result.put("wgs84", Map.of(
            "latitude", 37.5665,
            "longitude", 126.9780,
            "note", "This is a placeholder. Implement proper coordinate conversion."
        ));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 모든 병원의 좌표 재계산
     * POST /api/hospitals/recalculate-coordinates
     */
    @PostMapping("/recalculate-coordinates")
    public ResponseEntity<Map<String, Object>> recalculateCoordinates() {
        log.info("병원 좌표 재계산 시작");
        Map<String, Object> result = hospitalService.recalculateAllCoordinates();
        log.info("병원 좌표 재계산 완료: {}", result);
        return ResponseEntity.ok(result);
    }
}