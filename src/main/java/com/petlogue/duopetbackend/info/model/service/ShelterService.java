package com.petlogue.duopetbackend.info.model.service;

import com.petlogue.duopetbackend.info.jpa.entity.ShelterEntity;
import com.petlogue.duopetbackend.info.jpa.repository.ShelterRepository;
import com.petlogue.duopetbackend.info.model.dto.ShelterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("infoShelterService")
@Slf4j
@Transactional(readOnly = true)
public class ShelterService {

    private final ShelterRepository shelterRepository;
    
    public ShelterService(@Qualifier("infoShelterRepository") ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    /**
     * 모든 활성화된 보호소 조회 (DTO 응답)
     */
    public Page<ShelterDto.Response> getAllShelters(Pageable pageable) {
        List<Object[]> results = shelterRepository.findActiveShelters();
        
        // DTO로 변환
        List<ShelterDto.Response> responses = results.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // 수동으로 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        
        List<ShelterDto.Response> pageContent = responses.subList(start, end);
        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    /**
     * 페이징을 지원하는 활성화된 보호소 조회 (Entity 반환 - 호환성용)
     */
    public Page<ShelterEntity> getActiveSheltersWithPaging(Pageable pageable) {
        Page<Object[]> results = shelterRepository.findActiveSheltersWithPaging(pageable);
        List<ShelterEntity> shelters = results.getContent().stream()
                .map(this::mapToShelterEntity)
                .collect(Collectors.toList());
        return new PageImpl<>(shelters, pageable, results.getTotalElements());
    }

    /**
     * 모든 활성화된 보호소 조회 (Entity 반환 - 호환성용)
     */
    public List<ShelterEntity> getAllActiveShelters() {
        List<Object[]> results = shelterRepository.findActiveShelters();
        return results.stream()
                .map(this::mapToShelterEntity)
                .collect(Collectors.toList());
    }

    /**
     * 키워드로 보호소 검색 (DTO 응답)
     */
    public List<ShelterDto.Response> searchShelters(ShelterDto.SearchRequest searchRequest) {
        List<Object[]> results;
        
        // 키워드로 검색
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            results = shelterRepository.findActiveSheltersByKeywordSearch(searchRequest.getKeyword());
        } else {
            results = shelterRepository.findActiveShelters();
        }

        // DTO로 변환하면서 거리 계산
        List<ShelterDto.Response> responses = results.stream()
                .map(row -> convertToResponseWithDistance(row, 
                     searchRequest.getUserLatitude(), 
                     searchRequest.getUserLongitude()))
                .collect(Collectors.toList());

        // 보호소 타입 필터 적용
        if (searchRequest.getType() != null && !searchRequest.getType().trim().isEmpty()) {
            responses = responses.stream()
                    .filter(response -> searchRequest.getType().equals(response.getType()))
                    .collect(Collectors.toList());
        }

        // 최소 평점 필터 적용
        if (searchRequest.getMinRating() != null) {
            responses = responses.stream()
                    .filter(response -> response.getRating() != null && 
                           response.getRating() >= searchRequest.getMinRating())
                    .collect(Collectors.toList());
        }

        // 거리 필터 적용
        if (searchRequest.getMaxDistance() != null && searchRequest.getUserLatitude() != null) {
            responses = responses.stream()
                    .filter(response -> response.getDistance() == null || 
                           response.getDistance() <= searchRequest.getMaxDistance())
                    .collect(Collectors.toList());
        }

        // 정렬 적용
        responses = sortResults(responses, searchRequest.getSortBy(), searchRequest.getSortOrder());

        return responses;
    }

    /**
     * 키워드로 보호소 검색 (Entity 반환 - 호환성용)
     */
    public List<ShelterEntity> searchSheltersByKeyword(String keyword) {
        List<Object[]> results = shelterRepository.findActiveSheltersByKeywordSearch(keyword);
        return results.stream()
                .map(this::mapToShelterEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 보호소 상세 정보 조회 (DTO 응답)
     */
    public Optional<ShelterDto.Response> getShelterResponseById(Long shelterId) {
        List<Object[]> results = shelterRepository.findActiveShelterById(shelterId);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(convertToResponse(results.get(0)));
    }

    /**
     * 특정 보호소 상세 정보 조회 (Entity 반환 - 호환성용)
     */
    public ShelterEntity getShelterById(Long shelterId) {
        List<Object[]> results = shelterRepository.findActiveShelterById(shelterId);
        if (results.isEmpty()) {
            return null;
        }
        return mapToShelterEntity(results.get(0));
    }

    /**
     * 거리 기반 정렬 (프론트엔드에서 사용자 위치 기반으로 계산)
     * 여기서는 기본 정렬만 제공하고, 실제 거리 계산은 프론트엔드에서 수행
     */
    public List<ShelterEntity> getSheltersNearLocation(double lat, double lng, int radiusKm) {
        // 일단 모든 보호소를 반환하고, 프론트엔드에서 거리 계산 및 필터링
        return getAllActiveShelters();
    }

    /**
     * 디버깅용 메서드 - 데이터베이스 연결 및 데이터 확인
     */
    public String getDebugInfo() {
        try {
            List<Object[]> results = shelterRepository.findActiveShelters();
            StringBuilder debug = new StringBuilder();
            debug.append("=== 보호소 디버깅 정보 ===\n");
            debug.append("총 활성화된 보호소 수: ").append(results.size()).append("\n");
            
            if (!results.isEmpty()) {
                debug.append("첫 번째 보호소 정보:\n");
                Object[] first = results.get(0);
                for (int i = 0; i < first.length; i++) {
                    debug.append("  [").append(i).append("] ").append(first[i]).append("\n");
                }
            }
            
            return debug.toString();
        } catch (Exception e) {
            return "디버깅 중 오류 발생: " + e.getMessage();
        }
    }

    /**
     * Object[] 배열을 ShelterDto.Response로 변환하는 메서드
     */
    private ShelterDto.Response convertToResponse(Object[] row) {
        return convertToResponseWithDistance(row, null, null);
    }

    /**
     * Object[] 배열을 ShelterDto.Response로 변환하고 거리 계산
     */
    private ShelterDto.Response convertToResponseWithDistance(Object[] row, 
                                                            BigDecimal userLat, 
                                                            BigDecimal userLng) {
        // 기본 정보 추출
        Long shelterId = row[0] != null ? ((Number) row[0]).longValue() : null;
        Long userId = row[1] != null ? ((Number) row[1]).longValue() : null;
        String shelterName = (String) row[2];
        String phone = (String) row[3];
        String email = (String) row[4];
        String address = (String) row[5];
        String website = (String) row[6];
        Integer capacity = row[7] != null ? ((Number) row[7]).intValue() : null;
        String operatingHours = (String) row[8];
        String renameFilename = (String) row[9];
        String originalFilename = (String) row[10];
        String managerName = (String) row[11];
        String role = (String) row[12];
        String status = (String) row[13];

        // 임시 데이터 생성
        Double rating = 4.0 + Math.random(); // 4.0~5.0 임시 평점
        Integer currentAnimals = (int) (Math.random() * (capacity != null ? capacity * 0.8 : 30));
        
        // 보호소 유형 추론
        String type = inferShelterType(shelterName);
        
        // 거리 계산
        Double distance = null;
        if (userLat != null && userLng != null) {
            // 실제로는 보호소의 위도/경도가 필요하지만, 임시로 주소 기반 추정
            BigDecimal shelterLat = getEstimatedLatitude(address);
            BigDecimal shelterLng = getEstimatedLongitude(address);
            if (shelterLat != null && shelterLng != null) {
                distance = calculateDistance(
                    userLat.doubleValue(), userLng.doubleValue(),
                    shelterLat.doubleValue(), shelterLng.doubleValue()
                );
            }
        }

        return ShelterDto.Response.builder()
                .shelterId(shelterId)
                .userId(userId)
                .name(shelterName)
                .shelterName(shelterName)
                .phone(phone)
                .email(email)
                .address(address)
                .website(website)
                .capacity(capacity)
                .operatingHours(operatingHours)
                .renameFilename(renameFilename)
                .originalFilename(originalFilename)
                .managerName(managerName)
                .role(role)
                .status(status)
                .currentAnimals(currentAnimals)
                .rating(rating)
                .type(type)
                .distance(distance)
                .facilities(Arrays.asList("입양상담실", "의료실", "운동장", "보호실")) // 기본 시설
                .adoptionProcess(Arrays.asList("상담", "방문", "동물 선택", "입양 신청")) // 기본 절차
                .specialNeeds(Arrays.asList("건강검진", "예방접종", "중성화 수술")) // 기본 서비스
                .description(shelterName + "에서 사랑이 필요한 동물들을 보호하고 있습니다.")
                .latitude(getEstimatedLatitude(address))
                .longitude(getEstimatedLongitude(address))
                .createdAt(null) // 추후 추가 필요
                .updatedAt(null) // 추후 추가 필요
                .build();
    }

    /**
     * Object[] 배열을 ShelterEntity로 매핑하는 헬퍼 메서드 (호환성용)
     */
    private ShelterEntity mapToShelterEntity(Object[] row) {
        ShelterEntity shelter = new ShelterEntity();
        
        // 기본 보호소 정보
        shelter.setShelterId(row[0] != null ? ((Number) row[0]).longValue() : null);
        shelter.setUserId(row[1] != null ? ((Number) row[1]).longValue() : null);
        shelter.setShelterName((String) row[2]);
        shelter.setPhone((String) row[3]);
        shelter.setEmail((String) row[4]);
        shelter.setAddress((String) row[5]);
        shelter.setWebsite((String) row[6]);
        shelter.setCapacity(row[7] != null ? ((Number) row[7]).intValue() : null);
        shelter.setOperatingHours((String) row[8]);
        shelter.setRenameFilename((String) row[9]);
        shelter.setOriginalFilename((String) row[10]);
        
        // USERS 테이블에서 조인된 정보
        shelter.setManagerName((String) row[11]);
        shelter.setRole((String) row[12]);
        shelter.setStatus((String) row[13]);
        
        // 프론트엔드 호환성을 위한 추가 설정
        shelter.setName(shelter.getShelterName());
        shelter.setRating(4.0 + Math.random()); // 임시 평점 (4.0~5.0)
        shelter.setCurrentAnimals((int) (Math.random() * (shelter.getCapacity() != null ? shelter.getCapacity() * 0.8 : 30))); // 임시 현재 동물 수
        
        return shelter;
    }

    /**
     * 보호소 이름으로 타입 추론
     */
    private String inferShelterType(String shelterName) {
        if (shelterName == null) return "private";
        
        String lowerName = shelterName.toLowerCase();
        if (lowerName.contains("시") && (lowerName.contains("보호센터") || lowerName.contains("동물보호"))) {
            return "public"; // 공공보호소
        } else if (lowerName.contains("협회") || lowerName.contains("단체") || lowerName.contains("사단법인")) {
            return "organization"; // 단체보호소
        } else {
            return "private"; // 민간보호소
        }
    }

    /**
     * 주소로부터 대략적인 위도 추정
     */
    private BigDecimal getEstimatedLatitude(String address) {
        if (address == null) return BigDecimal.valueOf(37.5665); // 서울 기본값
        
        // 주요 도시별 대략적인 위도
        if (address.contains("서울")) return BigDecimal.valueOf(37.5665);
        if (address.contains("부산")) return BigDecimal.valueOf(35.1796);
        if (address.contains("대구")) return BigDecimal.valueOf(35.8714);
        if (address.contains("인천")) return BigDecimal.valueOf(37.4563);
        if (address.contains("광주")) return BigDecimal.valueOf(35.1595);
        if (address.contains("대전")) return BigDecimal.valueOf(36.3504);
        if (address.contains("울산")) return BigDecimal.valueOf(35.5384);
        if (address.contains("경기")) return BigDecimal.valueOf(37.4138);
        
        return BigDecimal.valueOf(37.5665); // 기본값
    }

    /**
     * 주소로부터 대략적인 경도 추정
     */
    private BigDecimal getEstimatedLongitude(String address) {
        if (address == null) return BigDecimal.valueOf(126.9780); // 서울 기본값
        
        // 주요 도시별 대략적인 경도
        if (address.contains("서울")) return BigDecimal.valueOf(126.9780);
        if (address.contains("부산")) return BigDecimal.valueOf(129.0756);
        if (address.contains("대구")) return BigDecimal.valueOf(128.6014);
        if (address.contains("인천")) return BigDecimal.valueOf(126.7052);
        if (address.contains("광주")) return BigDecimal.valueOf(126.8526);
        if (address.contains("대전")) return BigDecimal.valueOf(127.3845);
        if (address.contains("울산")) return BigDecimal.valueOf(129.3114);
        if (address.contains("경기")) return BigDecimal.valueOf(127.5183);
        
        return BigDecimal.valueOf(126.9780); // 기본값
    }

    /**
     * 거리 계산 (Haversine 공식)
     */
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

    /**
     * 검색 결과 정렬
     */
    private List<ShelterDto.Response> sortResults(List<ShelterDto.Response> responses, 
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
                            else result = Double.compare(a.getRating(), b.getRating());
                            break;
                        case "name":
                            result = a.getName().compareTo(b.getName());
                            break;
                        case "capacity":
                            if (a.getCapacity() == null && b.getCapacity() == null) result = 0;
                            else if (a.getCapacity() == null) result = 1;
                            else if (b.getCapacity() == null) result = -1;
                            else result = Integer.compare(a.getCapacity(), b.getCapacity());
                            break;
                        default:
                            result = 0;
                    }
                    return ascending ? result : -result;
                })
                .collect(Collectors.toList());
    }
}