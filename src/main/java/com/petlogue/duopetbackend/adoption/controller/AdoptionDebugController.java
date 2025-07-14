package com.petlogue.duopetbackend.adoption.controller;

import com.petlogue.duopetbackend.adoption.jpa.repository.AdoptionAnimalRepository;
import com.petlogue.duopetbackend.adoption.model.service.AdoptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/adoption/debug")
@RequiredArgsConstructor
public class AdoptionDebugController {
    
    private final AdoptionAnimalRepository adoptionAnimalRepository;
    private final AdoptionService adoptionService;

    @GetMapping("/test")
    public Map<String, Object> debugTest() {
        log.info("Debug endpoint reached!");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "If you see this, the endpoint is accessible");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/security-context")
    public Map<String, Object> checkSecurityContext() {
        log.info("Checking security context");
        Map<String, Object> response = new HashMap<>();
        
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        response.put("principal", authentication != null ? authentication.getPrincipal() : null);
        response.put("authorities", authentication != null ? authentication.getAuthorities() : null);
        
        return response;
    }
    
    @GetMapping("/check-api")
    public Map<String, Object> checkPublicApi() {
        log.info("Checking public API configuration");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 직접 URL 구성해서 확인 - API 수정일 이전 날짜 사용
            String testUrl = "http://apis.data.go.kr/1543061/abandonmentPublicSrvc/abandonmentPublic" +
                    "?serviceKey=MGb8WPXPe3xTmnO9v%2BXoQeQclING%2F0vIvYnowpC3OrRAwjPGJMCTI73sK3qT%2BT%2BPwY%2BqtMjE6sSAu74hQSwqhA%3D%3D" +
                    "&bgnde=20250430&endde=20250530&pageNo=1&numOfRows=10&_type=json";
            
            response.put("testUrl", testUrl);
            response.put("message", "브라우저에서 위 URL을 직접 열어보세요");
            response.put("note", "정상적인 경우 JSON 데이터가 표시됩니다");
            
            // DB에 있는 데이터 수 확인
            long count = adoptionAnimalRepository.count();
            response.put("currentDataCount", count);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/cleanup-age-data")
    public Map<String, Object> cleanupAgeData() {
        log.info("Starting age data cleanup");
        return adoptionService.cleanupInvalidAgeData();
    }
    
    @GetMapping("/test-age-parsing")
    public Map<String, Object> testAgeParsing() {
        log.info("Testing age parsing logic");
        Map<String, Object> result = new HashMap<>();
        
        // 테스트 케이스들
        String[] testCases = {
            "2024(년생)",
            "2023(년생)",
            "2020(년생)", 
            "2010(년생)",
            "3",
            "12개월",
            "6개월", 
            "24개월",
            "잘못된형식",
            null,
            "",
            "2024",
            "1995(년생)"
        };
        
        Map<String, Object> testResults = new HashMap<>();
        
        for (String testCase : testCases) {
            try {
                // PublicAnimalApiResponse의 parseAge 메서드를 테스트하기 위해
                // 임시 Item 객체 생성 후 DTO 변환을 통해 테스트
                com.petlogue.duopetbackend.adoption.model.dto.PublicAnimalApiResponse.Item item = 
                    new com.petlogue.duopetbackend.adoption.model.dto.PublicAnimalApiResponse.Item();
                item.setAge(testCase);
                item.setDesertionNo("TEST-" + (testCase != null ? testCase.replaceAll("[^a-zA-Z0-9]", "") : "null"));
                
                var dto = com.petlogue.duopetbackend.adoption.model.dto.PublicAnimalApiResponse.toDto(item);
                
                Map<String, Object> testResult = new HashMap<>();
                testResult.put("input", testCase);
                testResult.put("parsed_age", dto.getAge());
                testResult.put("success", dto.getAge() != null);
                
                testResults.put(testCase != null ? testCase : "null", testResult);
                
            } catch (Exception e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("input", testCase);
                errorResult.put("error", e.getMessage());
                errorResult.put("success", false);
                testResults.put(testCase != null ? testCase : "null", errorResult);
            }
        }
        
        result.put("test_results", testResults);
        result.put("current_year", java.time.Year.now().getValue());
        result.put("test_time", java.time.LocalDateTime.now().toString());
        
        return result;
    }
}