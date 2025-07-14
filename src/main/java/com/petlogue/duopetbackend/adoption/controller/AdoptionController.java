package com.petlogue.duopetbackend.adoption.controller;

import com.petlogue.duopetbackend.adoption.model.dto.AdoptionAnimalDto;
import com.petlogue.duopetbackend.adoption.model.service.AdoptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/adoption")
@RequiredArgsConstructor
public class AdoptionController {
    
    private final AdoptionService adoptionService;
    
    /**
     * 메인 화면용 랜덤 동물 조회
     */
    @GetMapping("/featured-animals")
    public ResponseEntity<List<AdoptionAnimalDto>> getFeaturedAnimals(
            @RequestParam(defaultValue = "10") int count) {
        
        List<AdoptionAnimalDto> animals = adoptionService.getRandomAnimalsForMain(count);
        return ResponseEntity.ok(animals);
    }
    
    /**
     * 입양 가능 동물 목록 조회
     */
    @GetMapping("/animals")
    public ResponseEntity<Page<AdoptionAnimalDto>> getAvailableAnimals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<AdoptionAnimalDto> animals = adoptionService.getAvailableAnimals(pageable);
        return ResponseEntity.ok(animals);
    }
    
    /**
     * 동물 상세 정보 조회
     */
    @GetMapping("/animals/{animalId}")
    public ResponseEntity<AdoptionAnimalDto> getAnimalDetail(@PathVariable Long animalId) {
        AdoptionAnimalDto animal = adoptionService.getAnimalDetail(animalId);
        return ResponseEntity.ok(animal);
    }
    
    /**
     * 동물 검색
     */
    @GetMapping("/animals/search")
    public ResponseEntity<Page<AdoptionAnimalDto>> searchAnimals(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String neutered,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<AdoptionAnimalDto> animals = adoptionService.searchAnimals(
                region, type, gender, neutered, pageable);
        return ResponseEntity.ok(animals);
    }
    
    /**
     * 데이터 동기화 수동 실행 (관리자용)
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncData() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> syncResult = adoptionService.syncAdoptionData();
            result.put("status", "success");
            result.put("message", "Data synchronization completed");
            result.putAll(syncResult);  // 동기화 통계 추가
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error starting data sync", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("type", e.getClass().getSimpleName());
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 디버깅용 - 동기화 테스트
     */
    @GetMapping("/sync/debug")
    public ResponseEntity<Map<String, Object>> debugSync() {
        return ResponseEntity.ok(adoptionService.debugSyncTest());
    }
    
    /**
     * 디버깅용 - 설정 확인
     */
    @GetMapping("/debug/config")
    public ResponseEntity<Map<String, Object>> debugConfig() {
        return ResponseEntity.ok(adoptionService.getDebugConfig());
    }
    
    /**
     * 디버깅용 - 직접 API 호출 테스트
     */
    @GetMapping("/debug/direct-test")
    public ResponseEntity<String> directTest() {
        return ResponseEntity.ok(adoptionService.directApiTest());
    }
    
    /**
     * 디버깅용 - 단일 동물 동기화 테스트
     */
    @GetMapping("/debug/sync-one")
    public ResponseEntity<Map<String, Object>> syncOne() {
        return ResponseEntity.ok(adoptionService.testSyncOne());
    }
    
    /**
     * 간단한 동기화 테스트
     */
    @GetMapping("/debug/simple-sync")
    public ResponseEntity<Map<String, Object>> simpleSync() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 동기화 시작
            adoptionService.syncAdoptionData();
            result.put("status", "started");
            result.put("message", "동기화가 시작되었습니다. 서버 로그를 확인하세요.");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("type", e.getClass().getSimpleName());
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 데이터베이스 현황 조회
     */
    @GetMapping("/debug/db-status")
    public ResponseEntity<Map<String, Object>> getDbStatus() {
        return ResponseEntity.ok(adoptionService.getDatabaseStatus());
    }
    
    /**
     * API 페이지네이션 테스트
     */
    @GetMapping("/debug/pagination")
    public ResponseEntity<Map<String, Object>> testPagination() {
        return ResponseEntity.ok(adoptionService.testPagination());
    }
    
}