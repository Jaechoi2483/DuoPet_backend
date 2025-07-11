package com.petlogue.duopetbackend.adoption.controller;

import com.petlogue.duopetbackend.adoption.model.service.PublicDataApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/adoption/test")
@RequiredArgsConstructor
public class AdoptionTestController {
    
    private final PublicDataApiClient publicDataApiClient;
    
    /**
     * 보호소 정보 테스트
     */
    @GetMapping("/shelters")
    public ResponseEntity<Map<String, Object>> testGetShelters(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) {
        
        try {
            Map<String, Object> result = publicDataApiClient.getShelterInfo(pageNo, numOfRows);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error calling shelter API", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 유기동물 정보 테스트
     */
    @GetMapping("/animals")
    public ResponseEntity<Map<String, Object>> testGetAnimals(
            @RequestParam(required = false) String upkind,
            @RequestParam(defaultValue = "protect") String state,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) {
        
        try {
            // API 수정일(2025-05-30) 이전 데이터 조회
            LocalDate endDate = LocalDate.of(2025, 5, 30);  // API 수정일
            LocalDate startDate = endDate.minusDays(30);    // 30일 전부터
            String bgnde = startDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            String endde = endDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            
            Map<String, Object> result = publicDataApiClient.getAbandonmentAnimals(
                    bgnde, endde, upkind, state, pageNo, numOfRows);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error calling animal API", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 보호소의 보호동물 조회 테스트
     */
    @GetMapping("/shelter/{careRegNo}/animals")
    public ResponseEntity<Map<String, Object>> testGetAnimalsByShelter(
            @PathVariable String careRegNo,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) {
        
        try {
            Map<String, Object> result = publicDataApiClient.getAnimalsByShelter(
                    careRegNo, pageNo, numOfRows);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error calling animals by shelter API", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}