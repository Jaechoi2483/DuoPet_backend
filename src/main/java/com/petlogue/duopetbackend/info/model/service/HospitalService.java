package com.petlogue.duopetbackend.info.model.service;

import com.petlogue.duopetbackend.info.jpa.entity.AnimalHospital;
import com.petlogue.duopetbackend.info.jpa.entity.HospitalEntity;
import com.petlogue.duopetbackend.info.jpa.repository.AnimalHospitalRepository;
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
    private final AnimalHospitalRepository animalHospitalRepository;

    /**
     * 모든 병원 조회 (ANIMAL_HOSPITALS 테이블에서)
     */
    public Page<HospitalDto.Response> getAllHospitals(Pageable pageable) {
        // ANIMAL_HOSPITALS 테이블에서 영업 중인 병원 조회
        Page<AnimalHospital> hospitals = animalHospitalRepository.findOperatingHospitals(pageable);
        
        return hospitals.map(this::convertAnimalHospitalToResponse);
    }

    /**
     * 병원 상세 조회
     */
    public Optional<HospitalDto.Response> getHospitalById(Long vetId) {
        // AnimalHospital 테이블에서 조회
        return animalHospitalRepository.findById(vetId)
                .map(this::convertAnimalHospitalToResponse);
    }

    /**
     * 복합 검색
     */
    public Page<HospitalDto.Response> searchHospitals(HospitalDto.SearchRequest searchRequest) {
        List<AnimalHospital> hospitals;
        
        // 키워드로 검색
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            // ANIMAL_HOSPITALS 테이블에서 키워드 검색
            Page<AnimalHospital> searchResults = animalHospitalRepository.searchByKeyword(
                searchRequest.getKeyword(), 
                PageRequest.of(0, 1000) // 최대 1000개 결과
            );
            hospitals = searchResults.getContent();
        } else {
            // 영업 중인 모든 병원 조회
            Page<AnimalHospital> allHospitals = animalHospitalRepository.findOperatingHospitals(
                PageRequest.of(0, 1000)
            );
            hospitals = allHospitals.getContent();
        }

        // DTO로 변환하면서 거리 계산
        List<HospitalDto.Response> responses = hospitals.stream()
                .map(hospital -> convertAnimalHospitalToResponseWithDistance(hospital, 
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
        // ANIMAL_HOSPITALS 테이블에서 영업 중인 병원 조회
        Page<AnimalHospital> hospitals = animalHospitalRepository.findOperatingHospitals(
            PageRequest.of(0, 100) // 상위 100개만
        );
        
        return hospitals.stream()
                .map(this::convertAnimalHospitalToSummary)
                .filter(summary -> summary.getIsEmergency() != null && summary.getIsEmergency())
                .collect(Collectors.toList());
    }

    /**
     * 24시간 병원 조회
     */
    public List<HospitalDto.Summary> get24HourHospitals() {
        // ANIMAL_HOSPITALS 테이블에서 영업 중인 병원 조회
        Page<AnimalHospital> hospitals = animalHospitalRepository.findOperatingHospitals(
            PageRequest.of(0, 100) // 상위 100개만
        );
        
        return hospitals.stream()
                .map(this::convertAnimalHospitalToSummary)
                .filter(summary -> summary.getOpenHours() != null && summary.getOpenHours().contains("24"))
                .collect(Collectors.toList());
    }

    /**
     * 위치 기반 병원 검색
     */
    public List<HospitalDto.Response> getHospitalsByLocation(BigDecimal latitude, BigDecimal longitude, 
                                                          Double radiusKm) {
        // ANIMAL_HOSPITALS 테이블에서 근처 병원 조회
        List<AnimalHospital> hospitals = animalHospitalRepository.findNearbyHospitals(
            latitude.doubleValue(), 
            longitude.doubleValue(), 
            radiusKm
        );
        
        return hospitals.stream()
                .map(hospital -> convertAnimalHospitalToResponseWithDistance(hospital, latitude, longitude))
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
    
    private HospitalDto.Response convertAnimalHospitalToResponse(AnimalHospital hospital) {
        return convertAnimalHospitalToResponseWithDistance(hospital, null, null);
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
    
    private HospitalDto.Summary convertAnimalHospitalToSummary(AnimalHospital hospital) {
        return HospitalDto.Summary.builder()
                .vetId(hospital.getHospitalId()) // hospitalId를 vetId로 사용
                .name(hospital.getBusinessName())
                .address(hospital.getRoadAddress() != null ? hospital.getRoadAddress() : hospital.getJibunAddress())
                .phone(hospital.getPhone())
                .specialization("종합진료") // 기본값
                .isEmergency(false) // 기본값
                .rating(new BigDecimal("4.5")) // 기본값
                .reviewCount(0)
                .openHours("09:00 - 18:00") // 기본값
                .build();
    }

    private HospitalDto.Response convertAnimalHospitalToResponseWithDistance(AnimalHospital hospital,
                                                                           BigDecimal userLat,
                                                                           BigDecimal userLng) {
        // 기본 서비스 목록
        List<String> services = Arrays.asList("진료", "건강검진", "예방접종");
        
        Double distance = null;
        if (userLat != null && userLng != null && 
            hospital.getLatitude() != null && hospital.getLongitude() != null) {
            distance = calculateDistance(
                userLat.doubleValue(), userLng.doubleValue(),
                hospital.getLatitude(), hospital.getLongitude()
            );
        }
        
        return HospitalDto.Response.builder()
                .vetId(hospital.getHospitalId()) // hospitalId를 vetId로 사용
                .name(hospital.getBusinessName())
                .licenseNumber(hospital.getManagementNo())
                .phone(hospital.getPhone())
                .email(null) // ANIMAL_HOSPITALS에는 이메일 정보가 없음
                .address(hospital.getRoadAddress() != null ? hospital.getRoadAddress() : hospital.getJibunAddress())
                .website(null)
                .specialization("종합진료") // 기본값
                .renameFilename(null)
                .originalFilename(null)
                .latitude(hospital.getLatitude() != null ? new BigDecimal(hospital.getLatitude()) : null)
                .longitude(hospital.getLongitude() != null ? new BigDecimal(hospital.getLongitude()) : null)
                .openHours("09:00 - 18:00") // 기본값
                .isEmergency(false) // 기본값
                .services(services)
                .rating(new BigDecimal("4.5")) // 기본값
                .reviewCount(0)
                .description("반려동물의 건강을 책임지는 " + hospital.getBusinessName() + "입니다.")
                .distance(distance)
                .createdAt(hospital.getCreatedAt())
                .updatedAt(hospital.getUpdatedAt())
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
            // ANIMAL_HOSPITALS 테이블 정보
            long animalHospitalCount = animalHospitalRepository.count();
            log.info("전체 ANIMAL_HOSPITALS 테이블 count: {}", animalHospitalCount);
            
            // 영업 중인 병원 수
            Page<AnimalHospital> operatingHospitals = animalHospitalRepository.findOperatingHospitals(
                PageRequest.of(0, 5)
            );
            log.info("영업 중인 병원 수: {}", operatingHospitals.getTotalElements());
            
            // 처음 5개 데이터 로그 출력
            int idx = 1;
            for (AnimalHospital hospital : operatingHospitals.getContent()) {
                log.info("데이터 {}: hospitalId={}, businessName={}, address={}, phone={}", 
                    idx++, hospital.getHospitalId(), hospital.getBusinessName(), 
                    hospital.getRoadAddress(), hospital.getPhone());
            }
            
            // 기존 USERS 테이블 정보 (참고용)
            long totalUserCount = hospitalRepository.count();
            List<HospitalEntity> vetUsers = hospitalRepository.findByRole("vet");
            
            return String.format(
                "DB 연결 성공!\n" +
                "ANIMAL_HOSPITALS 테이블: 전체 %d개, 영업중 %d개\n" +
                "USERS 테이블 (참고): 전체 %d개, role='vet' %d개", 
                animalHospitalCount, operatingHospitals.getTotalElements(),
                totalUserCount, vetUsers.size());
                
        } catch (Exception e) {
            log.error("데이터베이스 디버깅 실패", e);
            return "DB 디버깅 실패: " + e.getMessage();
        }
    }
}