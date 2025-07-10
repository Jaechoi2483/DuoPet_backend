package com.petlogue.duopetbackend.info.model.service;

import com.petlogue.duopetbackend.info.jpa.entity.HospitalEntity;
import com.petlogue.duopetbackend.info.jpa.repository.HospitalRepository;
import com.petlogue.duopetbackend.info.model.dto.HospitalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    /**
     * 모든 병원 조회 (role='vet'이고 활성 상태)
     */
    public Page<HospitalDto.Response> getAllHospitals(Pageable pageable) {
        // role='vet'이고 status='active'인 병원만 조회
        List<HospitalEntity> entities = hospitalRepository.findByRoleAndStatus("vet", "active");
        
        // 수동으로 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), entities.size());
        
        List<HospitalEntity> pageContent = entities.subList(start, end);
        Page<HospitalEntity> page = new PageImpl<>(pageContent, pageable, entities.size());
        
        return page.map(this::convertToResponse);
    }

    /**
     * 병원 상세 조회
     */
    public Optional<HospitalDto.Response> getHospitalById(Long vetId) {
        return hospitalRepository.findById(vetId)
                .map(this::convertToResponse);
    }

    /**
     * 복합 검색
     */
    public Page<HospitalDto.Response> searchHospitals(HospitalDto.SearchRequest searchRequest) {
        List<HospitalEntity> entities;
        
        // 키워드로 검색 (role='vet' 조건 포함)
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            entities = hospitalRepository.findActiveVetsByKeywordSearch(searchRequest.getKeyword());
        } else {
            entities = hospitalRepository.findByRoleAndStatus("vet", "active");
        }

        // DTO로 변환하면서 거리 계산
        List<HospitalDto.Response> responses = entities.stream()
                .map(entity -> convertToResponseWithDistance(entity, 
                     searchRequest.getUserLatitude(), 
                     searchRequest.getUserLongitude()))
                .collect(Collectors.toList());

        // 서비스 필터 적용 (기본값이 있으므로 필터링 가능)
        if (searchRequest.getService() != null && !searchRequest.getService().trim().isEmpty()) {
            responses = responses.stream()
                    .filter(response -> response.getServices() != null && 
                           response.getServices().contains(searchRequest.getService()))
                    .collect(Collectors.toList());
        }

        // 응급병원 필터 적용
        if (searchRequest.getIsEmergency() != null && searchRequest.getIsEmergency()) {
            responses = responses.stream()
                    .filter(response -> response.getIsEmergency() != null && response.getIsEmergency())
                    .collect(Collectors.toList());
        }

        // 최소 평점 필터 적용
        if (searchRequest.getMinRating() != null) {
            responses = responses.stream()
                    .filter(response -> response.getRating() != null && 
                           response.getRating().compareTo(searchRequest.getMinRating()) >= 0)
                    .collect(Collectors.toList());
        }

        // 거리 필터 적용
        if (searchRequest.getMaxDistance() != null && searchRequest.getUserLatitude() != null) {
            responses = responses.stream()
                    .filter(response -> response.getDistance() == null || 
                           response.getDistance() <= searchRequest.getMaxDistance())
                    .collect(Collectors.toList());
        }

        // 정렬
        responses = sortResults(responses, searchRequest.getSortBy(), searchRequest.getSortOrder());

        // 페이징 처리
        return createPageFromList(responses, 
                                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                                searchRequest.getSize() != null ? searchRequest.getSize() : 10);
    }

    /**
     * 응급 병원 조회
     */
    public List<HospitalDto.Summary> getEmergencyHospitals() {
        // 활성 상태인 모든 병원을 조회한 후 응급병원 필터링
        List<HospitalEntity> entities = hospitalRepository.findByStatus("active");
        return entities.stream()
                .map(this::convertToSummary)
                .filter(summary -> summary.getIsEmergency() != null && summary.getIsEmergency())
                .collect(Collectors.toList());
    }

    /**
     * 24시간 병원 조회
     */
    public List<HospitalDto.Summary> get24HourHospitals() {
        // 활성 상태인 모든 병원을 조회한 후 24시간 병원 필터링
        List<HospitalEntity> entities = hospitalRepository.findByStatus("active");
        return entities.stream()
                .map(this::convertToSummary)
                .filter(summary -> summary.getOpenHours() != null && summary.getOpenHours().contains("24"))
                .collect(Collectors.toList());
    }

    /**
     * 위치 기반 병원 검색
     */
    public List<HospitalDto.Response> getHospitalsByLocation(BigDecimal latitude, BigDecimal longitude, 
                                                          Double radiusKm) {
        // 현재는 위치 정보가 @Transient이므로 모든 활성 병원을 조회
        List<HospitalEntity> entities = hospitalRepository.findByStatus("active");
        
        return entities.stream()
                .map(entity -> convertToResponseWithDistance(entity, latitude, longitude))
                .filter(response -> response.getDistance() == null || response.getDistance() <= radiusKm)
                .sorted((a, b) -> {
                    if (a.getDistance() == null && b.getDistance() == null) return 0;
                    if (a.getDistance() == null) return 1;
                    if (b.getDistance() == null) return -1;
                    return Double.compare(a.getDistance(), b.getDistance());
                })
                .collect(Collectors.toList());
    }

    private HospitalDto.Response convertToResponse(HospitalEntity entity) {
        return convertToResponseWithDistance(entity, null, null);
    }

    private HospitalDto.Response convertToResponseWithDistance(HospitalEntity entity, 
                                                             BigDecimal userLat, 
                                                             BigDecimal userLng) {
        List<String> services = null;
        if (entity.getServices() != null && !entity.getServices().trim().isEmpty()) {
            services = Arrays.asList(entity.getServices().split(","))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        Double distance = null;
        if (userLat != null && userLng != null && 
            entity.getLatitude() != null && entity.getLongitude() != null) {
            distance = calculateDistance(
                userLat.doubleValue(), userLng.doubleValue(),
                entity.getLatitude().doubleValue(), entity.getLongitude().doubleValue()
            );
        }

        return HospitalDto.Response.builder()
                .vetId(entity.getUserId()) // userId를 vetId로 사용
                .name(entity.getHospitalName()) // nickname(병원명)을 name으로 사용
                .licenseNumber(null) // USERS 테이블에는 없는 정보
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .website(null) // USERS 테이블에는 없는 정보
                .specialization(entity.getSpecialization())
                .renameFilename(entity.getRenameFilename())
                .originalFilename(entity.getOriginalFilename())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .openHours(entity.getOpenHours())
                .isEmergency(entity.getIsEmergency())
                .services(services)
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .description(entity.getDescription())
                .distance(distance)
                .createdAt(entity.getCreatedAt())
                .updatedAt(null) // USERS 테이블에는 updated_at이 없음
                .build();
    }

    private HospitalDto.Summary convertToSummary(HospitalEntity entity) {
        return HospitalDto.Summary.builder()
                .vetId(entity.getUserId()) // userId를 vetId로 사용
                .name(entity.getHospitalName()) // nickname(병원명)을 name으로 사용
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .specialization(entity.getSpecialization())
                .isEmergency(entity.getIsEmergency())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .openHours(entity.getOpenHours())
                .build();
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private List<HospitalDto.Response> sortResults(List<HospitalDto.Response> responses, 
                                                 String sortBy, String sortOrder) {
        final String finalSortBy = sortBy != null ? sortBy : "distance";
        final String finalSortOrder = sortOrder != null ? sortOrder : "asc";

        boolean ascending = "asc".equalsIgnoreCase(finalSortOrder);

        return responses.stream()
                .sorted((a, b) -> {
                    int result = 0;
                    switch (finalSortBy.toLowerCase()) {
                        case "distance":
                            if (a.getDistance() == null && b.getDistance() == null) result = 0;
                            else if (a.getDistance() == null) result = 1;
                            else if (b.getDistance() == null) result = -1;
                            else result = Double.compare(a.getDistance(), b.getDistance());
                            break;
                        case "rating":
                            if (a.getRating() == null && b.getRating() == null) result = 0;
                            else if (a.getRating() == null) result = 1;
                            else if (b.getRating() == null) result = -1;
                            else result = a.getRating().compareTo(b.getRating());
                            break;
                        case "name":
                            result = a.getName().compareTo(b.getName());
                            break;
                        default:
                            result = 0;
                    }
                    return ascending ? result : -result;
                })
                .collect(Collectors.toList());
    }

    private Page<HospitalDto.Response> createPageFromList(List<HospitalDto.Response> list, 
                                                        int page, int size) {
        int start = page * size;
        int end = Math.min(start + size, list.size());
        
        if (start >= list.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        }
        
        List<HospitalDto.Response> pageContent = list.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, size), list.size());
    }

    /**
     * 디버깅용 데이터베이스 정보 조회
     */
    public String getDebugInfo() {
        try {
            // 전체 사용자 수
            long totalCount = hospitalRepository.count();
            log.info("전체 USERS 테이블 count: {}", totalCount);
            
            // role='vet'인 사용자 수
            List<HospitalEntity> vetUsers = hospitalRepository.findByRole("vet");
            log.info("role='vet' 사용자 수: {}", vetUsers.size());
            
            // role='vet'이고 status='active'인 사용자 수
            List<HospitalEntity> activeVetUsers = hospitalRepository.findByRoleAndStatus("vet", "active");
            log.info("role='vet'이고 active 상태인 사용자 수: {}", activeVetUsers.size());
            
            // 처음 5개 데이터 로그 출력
            for (int i = 0; i < Math.min(5, activeVetUsers.size()); i++) {
                HospitalEntity entity = activeVetUsers.get(i);
                log.info("데이터 {}: userId={}, hospitalName={}, role={}, status={}", 
                    i+1, entity.getUserId(), entity.getHospitalName(), entity.getRole(), entity.getStatus());
            }
            
            return String.format(
                "DB 연결 성공! 전체 사용자: %d개, role='vet': %d개, active 상태 병원: %d개", 
                totalCount, vetUsers.size(), activeVetUsers.size());
                
        } catch (Exception e) {
            log.error("데이터베이스 디버깅 실패", e);
            return "DB 디버깅 실패: " + e.getMessage();
        }
    }
}