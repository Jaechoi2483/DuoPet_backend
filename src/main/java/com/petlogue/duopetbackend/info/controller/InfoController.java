package com.petlogue.duopetbackend.info.controller;

import com.petlogue.duopetbackend.info.model.dto.HospitalDto;
import com.petlogue.duopetbackend.info.model.dto.ShelterDto;
import com.petlogue.duopetbackend.info.model.dto.ShelterInfoDto;
import com.petlogue.duopetbackend.info.model.service.HospitalService;
import com.petlogue.duopetbackend.info.model.service.ShelterInfoService;
import com.petlogue.duopetbackend.info.model.service.ShelterService;
import com.petlogue.duopetbackend.info.model.service.AnimalHospitalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/info")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class InfoController {

    private final HospitalService hospitalService;
    private final ShelterService shelterService;
    private final ShelterInfoService shelterInfoService;
    private final AnimalHospitalService animalHospitalService;

    public InfoController(HospitalService hospitalService, 
                         @Qualifier("infoShelterService") ShelterService shelterService,
                         ShelterInfoService shelterInfoService,
                         AnimalHospitalService animalHospitalService) {
        this.hospitalService = hospitalService;
        this.shelterService = shelterService;
        this.shelterInfoService = shelterInfoService;
        this.animalHospitalService = animalHospitalService;
    }

    /**
     * 모든 병원 조회 (페이징)
     * GET /api/info/hospitals?page=0&size=10
     */
    @GetMapping("/hospitals")
    public ResponseEntity<Page<HospitalDto.Response>> getAllHospitals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("전체 병원 목록 조회 요청 - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<HospitalDto.Response> hospitals = hospitalService.getAllHospitals(pageable);
        
        log.info("병원 {}개 조회 완료", hospitals.getTotalElements());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(hospitals);
    }

    /**
     * 병원 상세 조회
     * GET /api/info/hospitals/{userId}
     */
    @GetMapping("/hospitals/{userId}")
    public ResponseEntity<HospitalDto.Response> getHospitalById(@PathVariable Long userId) {
        log.info("병원 상세 정보 조회 요청 - userId: {}", userId);
        
        return hospitalService.getHospitalById(userId)
                .map(hospital -> {
                    log.info("병원 정보 조회 성공 - {}", hospital.getName());
                    return ResponseEntity.ok(hospital);
                })
                .orElseGet(() -> {
                    log.warn("병원 정보를 찾을 수 없음 - userId: {}", userId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 병원 검색 (복합 검색)
     * POST /api/info/hospitals/search
     */
    @PostMapping("/hospitals/search")
    public ResponseEntity<Page<HospitalDto.Response>> searchHospitals(
            @RequestBody HospitalDto.SearchRequest searchRequest) {
        
        log.info("병원 검색 요청 - keyword: {}, emergency: {}, service: {}", 
                searchRequest.getKeyword(), searchRequest.getIsEmergency(), searchRequest.getService());
        
        Page<HospitalDto.Response> hospitals = hospitalService.searchHospitals(searchRequest);
        
        log.info("검색 결과: {}개 병원", hospitals.getTotalElements());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(hospitals);
    }

    /**
     * 간단한 키워드 검색 (GET 방식)
     * GET /api/info/hospitals/search?keyword=서울&emergency=true&service=진료
     */
    @GetMapping("/hospitals/search")
    public ResponseEntity<Page<HospitalDto.Response>> searchHospitalsSimple(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean emergency,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal userLat,
            @RequestParam(required = false) BigDecimal userLng,
            @RequestParam(required = false) Integer maxDistance,
            @RequestParam(defaultValue = "distance") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("간단 검색 요청 - keyword: {}, emergency: {}", keyword, emergency);
        
        HospitalDto.SearchRequest searchRequest = HospitalDto.SearchRequest.builder()
                .keyword(keyword)
                .isEmergency(emergency)
                .service(service)
                .minRating(minRating)
                .userLatitude(userLat)
                .userLongitude(userLng)
                .maxDistance(maxDistance)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();
        
        Page<HospitalDto.Response> hospitals = hospitalService.searchHospitals(searchRequest);
        
        log.info("간단 검색 결과: {}개 병원", hospitals.getTotalElements());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(hospitals);
    }

    /**
     * 응급 병원 조회
     * GET /api/info/hospitals/emergency
     */
    @GetMapping("/hospitals/emergency")
    public ResponseEntity<List<HospitalDto.Summary>> getEmergencyHospitals() {
        log.info("응급 병원 목록 조회 요청");
        
        List<HospitalDto.Summary> emergencyHospitals = hospitalService.getEmergencyHospitals();
        
        log.info("응급 병원 {}개 조회 완료", emergencyHospitals.size());
        return ResponseEntity.ok(emergencyHospitals);
    }

    /**
     * 24시간 병원 조회
     * GET /api/info/hospitals/24hour
     */
    @GetMapping("/hospitals/24hour")
    public ResponseEntity<List<HospitalDto.Summary>> get24HourHospitals() {
        log.info("24시간 병원 목록 조회 요청");
        
        List<HospitalDto.Summary> hospitals24h = hospitalService.get24HourHospitals();
        
        log.info("24시간 병원 {}개 조회 완료", hospitals24h.size());
        return ResponseEntity.ok(hospitals24h);
    }

    /**
     * 위치 기반 병원 검색
     * GET /api/info/hospitals/nearby?lat=37.5665&lng=126.9780&radius=5
     */
    @GetMapping("/hospitals/nearby")
    public ResponseEntity<List<HospitalDto.Response>> getNearbyHospitals(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "10") Double radius) {
        
        log.info("위치 기반 병원 검색 요청 - lat: {}, lng: {}, radius: {}km", lat, lng, radius);
        
        List<HospitalDto.Response> nearbyHospitals = hospitalService.getHospitalsByLocation(lat, lng, radius);
        
        log.info("반경 {}km 내 병원 {}개 조회 완료", radius, nearbyHospitals.size());
        return ResponseEntity.ok(nearbyHospitals);
    }

    /**
     * CSV 파일 임포트 (관리자 전용)
     * POST /api/info/hospitals/import/csv
     * AnimalHospitalController에서 이전한 기능
     * 관리자 페이지의 데이터 동기화에서 사용 가능
     */
    @PostMapping("/hospitals/import/csv")
    public ResponseEntity<?> importHospitalsCsv(@RequestParam("file") MultipartFile file) {
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
            
            log.info("병원 CSV 임포트 시작 - 파일명: {}", filename);
            
            // AnimalHospitalService의 importFromCsv 메소드 사용
            Map<String, Object> result = animalHospitalService.importFromCsv(file);
            
            log.info("병원 CSV 임포트 완료 - 결과: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("병원 CSV 임포트 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "파일 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 근처 병원 찾기 (AnimalHospitalController에서 이전)
     * GET /api/info/hospitals/nearby/detail?lat=37.5665&lng=126.9780&radius=5
     * 더 상세한 정보와 거리 계산을 포함한 버전
     */
    @GetMapping("/hospitals/nearby/detail")
    public ResponseEntity<?> getNearbyHospitalsDetail(
            @RequestParam("lat") Double latitude,
            @RequestParam("lng") Double longitude,
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
        
        log.info("상세 근처 병원 검색 - lat: {}, lng: {}, radius: {}km", latitude, longitude, radius);
        
        // AnimalHospitalService의 getNearbyHospitals 메소드 사용
        var nearbyHospitals = animalHospitalService.getNearbyHospitals(latitude, longitude, radius);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", nearbyHospitals.size());
        response.put("radius", radius);
        response.put("hospitals", nearbyHospitals);
        
        log.info("상세 근처 병원 검색 완료 - {}개 발견", nearbyHospitals.size());
        return ResponseEntity.ok(response);
    }

    /**
     * API 상태 확인
     * GET /api/info/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Info API is running");
    }

    /**
     * 데이터베이스 연결 및 데이터 확인
     * GET /api/info/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> testDatabase() {
        log.info("데이터베이스 테스트 시작");
        
        try {
            String debugInfo = hospitalService.getDebugInfo();
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            log.error("데이터베이스 테스트 실패", e);
            return ResponseEntity.status(500).body("DB 테스트 실패: " + e.getMessage());
        }
    }

    // ===== 보호소 관련 API =====

    /**
     * 모든 보호소 조회 (페이징) - DTO 기반
     * GET /api/info/shelters?page=0&size=10
     */
    @GetMapping("/shelters")
    public ResponseEntity<Page<ShelterDto.Response>> getAllShelters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("전체 보호소 목록 조회 요청 - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ShelterDto.Response> shelters = shelterService.getAllShelters(pageable);
        
        log.info("보호소 {}개 조회 완료", shelters.getTotalElements());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(shelters);
    }

    /**
     * 보호소 상세 조회 - DTO 기반
     * GET /api/info/shelters/{shelterId}
     */
    @GetMapping("/shelters/{shelterId}")
    public ResponseEntity<ShelterDto.Response> getShelterById(@PathVariable Long shelterId) {
        log.info("보호소 상세 정보 조회 요청 - shelterId: {}", shelterId);
        
        return shelterService.getShelterResponseById(shelterId)
                .map(shelter -> {
                    log.info("보호소 정보 조회 성공 - {}", shelter.getName());
                    return ResponseEntity.ok()
                            .header("Cache-Control", "no-cache, no-store, must-revalidate")
                            .header("Pragma", "no-cache")
                            .header("Expires", "0")
                            .body(shelter);
                })
                .orElseGet(() -> {
                    log.warn("보호소 정보를 찾을 수 없음 - shelterId: {}", shelterId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 보호소 검색 (키워드 기반) - DTO 기반
     * GET /api/info/shelters/search?keyword=서울&type=public&minRating=4.0&userLat=37.5665&userLng=126.9780&maxDistance=10&sortBy=distance&sortOrder=asc&size=50
     */
    @GetMapping("/shelters/search")
    public ResponseEntity<List<ShelterDto.Response>> searchShelters(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String managerName,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal userLat,
            @RequestParam(required = false) BigDecimal userLng,
            @RequestParam(required = false) Integer maxDistance,
            @RequestParam(defaultValue = "distance") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("보호소 검색 요청 - keyword: {}, type: {}", keyword, type);
        
        ShelterDto.SearchRequest searchRequest = ShelterDto.SearchRequest.builder()
                .keyword(keyword)
                .type(type)
                .managerName(managerName)
                .minCapacity(minCapacity)
                .maxCapacity(maxCapacity)
                .minRating(minRating)
                .userLatitude(userLat)
                .userLongitude(userLng)
                .maxDistance(maxDistance)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .size(size)
                .build();
        
        List<ShelterDto.Response> shelters = shelterService.searchShelters(searchRequest);
        
        // size 제한 적용
        if (shelters.size() > size) {
            shelters = shelters.subList(0, size);
        }
        
        log.info("보호소 검색 결과: {}개", shelters.size());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(shelters);
    }

    /**
     * 위치 기반 보호소 검색 - DTO 기반
     * GET /api/info/shelters/nearby?lat=37.5665&lng=126.9780&radius=10
     */
    @GetMapping("/shelters/nearby")
    public ResponseEntity<List<ShelterDto.Response>> getNearbyShelters(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "10") Integer radius) {
        
        log.info("위치 기반 보호소 검색 요청 - lat: {}, lng: {}, radius: {}km", lat, lng, radius);
        
        ShelterDto.SearchRequest searchRequest = ShelterDto.SearchRequest.builder()
                .userLatitude(lat)
                .userLongitude(lng)
                .maxDistance(radius)
                .sortBy("distance")
                .sortOrder("asc")
                .build();
        
        List<ShelterDto.Response> nearbyShelters = shelterService.searchShelters(searchRequest);
        
        log.info("반경 {}km 내 보호소 {}개 조회 완료", radius, nearbyShelters.size());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(nearbyShelters);
    }

    /**
     * 보호소 요약 정보 조회 (메인 페이지용)
     * GET /api/info/shelters/summary
     */
    @GetMapping("/shelters/summary")
    public ResponseEntity<List<ShelterDto.Summary>> getShelterSummary() {
        log.info("보호소 요약 정보 조회 요청");
        
        try {
            // 모든 보호소를 가져와서 Summary로 변환
            Pageable pageable = PageRequest.of(0, 10); // 상위 10개만
            Page<ShelterDto.Response> shelters = shelterService.getAllShelters(pageable);
            
            List<ShelterDto.Summary> summaries = shelters.getContent().stream()
                    .map(shelter -> ShelterDto.Summary.builder()
                            .shelterId(shelter.getShelterId())
                            .name(shelter.getName())
                            .address(shelter.getAddress())
                            .phone(shelter.getPhone())
                            .type(shelter.getType())
                            .capacity(shelter.getCapacity())
                            .currentAnimals(shelter.getCurrentAnimals())
                            .rating(shelter.getRating())
                            .operatingHours(shelter.getOperatingHours())
                            .distance(shelter.getDistance())
                            .managerName(shelter.getManagerName())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("보호소 요약 정보 {}개 조회 완료", summaries.size());
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            log.error("보호소 요약 정보 조회 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 보호소 데이터베이스 테스트
     * GET /api/info/shelters/test
     */
    @GetMapping("/shelters/test")
    public ResponseEntity<String> testShelterDatabase() {
        log.info("보호소 데이터베이스 테스트 시작");
        
        try {
            String debugInfo = shelterService.getDebugInfo();
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            log.error("보호소 데이터베이스 테스트 실패", e);
            return ResponseEntity.status(500).body("보호소 DB 테스트 실패: " + e.getMessage());
        }
    }
    
    // =============== 공공데이터 보호소 조회 API ===============
    
    /**
     * 공공데이터 보호소 전체 조회 (페이징)
     * GET /api/info/shelters/public?page=0&size=20
     */
    @GetMapping("/shelters/public")
    public ResponseEntity<Page<ShelterInfoDto>> getPublicShelters(
            // ✅ @PageableDefault를 사용하여 size 기본값을 50으로 설정
            @PageableDefault(size = 50, sort = "careNm") Pageable pageable) {

        log.info("공공데이터 보호소 목록 조회 요청 - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // PageRequest.of()를 직접 생성할 필요 없이 pageable을 바로 전달
        Page<ShelterInfoDto> shelters = shelterInfoService.getAllShelters(pageable);

        log.info("공공 보호소 {}개 조회 완료", shelters.getTotalElements());
        return ResponseEntity.ok(shelters);
    }
    
    /**
     * 공공데이터 보호소 상세 조회
     * GET /api/info/shelters/public/{shelterInfoId}
     */
    @GetMapping("/shelters/public/{shelterInfoId}")
    public ResponseEntity<ShelterInfoDto> getPublicShelterById(@PathVariable Long shelterInfoId) {
        log.info("공공데이터 보호소 상세 조회 요청 - ID: {}", shelterInfoId);
        
        return shelterInfoService.getShelterById(shelterInfoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 공공데이터 보호소 검색
     * GET /api/info/shelters/public/search?keyword=서울&orgNm=&divisionNm=법인&animal=개
     */
    @GetMapping("/shelters/public/search")
    public ResponseEntity<Page<ShelterInfoDto>> searchPublicShelters(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgNm,
            @RequestParam(required = false) String divisionNm,
            @RequestParam(required = false) String animal,
            // ✅ @PageableDefault를 사용하여 size 기본값을 50으로 설정
            @PageableDefault(size = 50, sort = "careNm") Pageable pageable) {

        log.info("공공데이터 보호소 검색 - keyword: {}, ...", keyword);

        // pageable 객체를 서비스로 바로 전달
        Page<ShelterInfoDto> shelters = shelterInfoService.searchShelters(
                keyword, orgNm, divisionNm, animal, pageable);

        log.info("검색 결과 {}개 조회 완료", shelters.getTotalElements());
        return ResponseEntity.ok(shelters);
    }
    
    /**
     * 위치 기반 근처 공공 보호소 조회
     * GET /api/info/shelters/public/nearby?lat=37.5665&lng=126.9780&radius=10
     */
    @GetMapping("/shelters/public/nearby")
    public ResponseEntity<List<ShelterInfoDto>> getNearbyPublicShelters(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10") Double radius) {
        
        log.info("근처 공공 보호소 조회 - 위치: ({}, {}), 반경: {}km", lat, lng, radius);
        
        List<ShelterInfoDto> shelters = shelterInfoService.getNearbyShelters(lat, lng, radius);
        
        log.info("반경 {}km 내 {}개 보호소 조회 완료", radius, shelters.size());
        return ResponseEntity.ok(shelters);
    }
    
    /**
     * 지역별 공공 보호소 조회
     * GET /api/info/shelters/public/region?region=서울
     */
    @GetMapping("/shelters/public/region")
    public ResponseEntity<Page<ShelterInfoDto>> getPublicSheltersByRegion(
            @RequestParam String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("지역별 공공 보호소 조회 - 지역: {}", region);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ShelterInfoDto> shelters = shelterInfoService.getSheltersByRegion(region, pageable);
        
        log.info("{}지역 보호소 {}개 조회 완료", region, shelters.getTotalElements());
        return ResponseEntity.ok(shelters);
    }
    
    /**
     * 동물 종류별 공공 보호소 조회
     * GET /api/info/shelters/public/animal?type=개
     */
    @GetMapping("/shelters/public/animal")
    public ResponseEntity<Page<ShelterInfoDto>> getPublicSheltersByAnimalType(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("동물 종류별 공공 보호소 조회 - 동물: {}", type);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ShelterInfoDto> shelters = shelterInfoService.getSheltersByAnimalType(type, pageable);
        
        log.info("{} 수용 가능 보호소 {}개 조회 완료", type, shelters.getTotalElements());
        return ResponseEntity.ok(shelters);
    }
    
    /**
     * 공공 보호소 통계 정보 조회
     * GET /api/info/shelters/public/statistics
     */
    @GetMapping("/shelters/public/statistics")
    public ResponseEntity<ShelterInfoService.ShelterStatisticsDto> getPublicShelterStatistics() {
        log.info("공공 보호소 통계 정보 조회");
        
        ShelterInfoService.ShelterStatisticsDto statistics = shelterInfoService.getShelterStatistics();
        
        log.info("공공 보호소 통계 조회 완료 - 총 {}개", statistics.getTotalCount());
        return ResponseEntity.ok(statistics);
    }
}