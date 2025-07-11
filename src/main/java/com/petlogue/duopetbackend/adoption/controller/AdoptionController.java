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
    public ResponseEntity<String> syncData() {
        try {
            adoptionService.syncAdoptionData();
            return ResponseEntity.ok("Data synchronization started");
        } catch (Exception e) {
            log.error("Error starting data sync", e);
            return ResponseEntity.internalServerError().body("Failed to start sync");
        }
    }
}