package com.petlogue.duopetbackend.info.model.service;

import com.petlogue.duopetbackend.info.jpa.entity.ShelterInfo;
import com.petlogue.duopetbackend.info.jpa.repository.ShelterInfoRepository;
import com.petlogue.duopetbackend.info.model.dto.ShelterInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 공공데이터 보호소 정보 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShelterInfoService {
    
    private final ShelterInfoRepository shelterInfoRepository;
    
    /**
     * 전체 보호소 목록 조회 (페이징)
     */
    public Page<ShelterInfoDto> getAllShelters(Pageable pageable) {
        Page<ShelterInfo> shelterPage = shelterInfoRepository.findAll(
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                          Sort.by(Sort.Direction.ASC, "careNm"))
        );
        
        return shelterPage.map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 보호소 상세 정보 조회
     */
    public Optional<ShelterInfoDto> getShelterById(Long shelterInfoId) {
        return shelterInfoRepository.findById(shelterInfoId)
                .map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 관리번호로 보호소 조회
     */
    public Optional<ShelterInfoDto> getShelterByCareRegNo(String careRegNo) {
        return shelterInfoRepository.findByCareRegNo(careRegNo)
                .map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 보호소명으로 검색 (부분 일치)
     */
    public Page<ShelterInfoDto> searchByShelterName(String keyword, Pageable pageable) {
        Page<ShelterInfo> shelterPage = shelterInfoRepository.findByCareNmContaining(keyword, pageable);
        return shelterPage.map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 지역별 보호소 조회
     */
    public Page<ShelterInfoDto> getSheltersByRegion(String region, Pageable pageable) {
        Page<ShelterInfo> shelterPage = shelterInfoRepository.findByRegion(region, pageable);
        return shelterPage.map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 관할기관별 보호소 조회
     */
    public Page<ShelterInfoDto> getSheltersByOrgNm(String orgNm, Pageable pageable) {
        Page<ShelterInfo> shelterPage = shelterInfoRepository.findByOrgNm(orgNm, pageable);
        return shelterPage.map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 보호소 구분별 조회
     */
    public Page<ShelterInfoDto> getSheltersByDivision(String divisionNm, Pageable pageable) {
        Page<ShelterInfo> shelterPage = shelterInfoRepository.findByDivisionNm(divisionNm, pageable);
        return shelterPage.map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 동물 종류별 보호소 조회
     */
    public Page<ShelterInfoDto> getSheltersByAnimalType(String animalType, Pageable pageable) {
        Page<ShelterInfo> shelterPage = shelterInfoRepository.findByAnimalType(animalType, pageable);
        return shelterPage.map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 위치 기반 근처 보호소 조회
     */
    public List<ShelterInfoDto> getNearbyShelters(Double lat, Double lng, Double radiusKm) {
        List<ShelterInfo> nearbyShelters = shelterInfoRepository.findNearbySheleters(lat, lng, radiusKm);
        
        return nearbyShelters.stream()
                .map(shelter -> {
                    ShelterInfoDto dto = ShelterInfoDto.fromEntity(shelter);
                    // 거리 계산 (Haversine formula)
                    double distance = calculateDistance(lat, lng, shelter.getLat(), shelter.getLng());
                    dto.setDistance(distance);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 복합 검색
     */
    public Page<ShelterInfoDto> searchShelters(String keyword, String orgNm, 
                                              String divisionNm, String animal, 
                                              Pageable pageable) {
        Page<ShelterInfo> shelterPage = shelterInfoRepository.searchShelters(
            keyword, orgNm, divisionNm, animal, pageable
        );
        
        return shelterPage.map(ShelterInfoDto::fromEntity);
    }
    
    /**
     * 두 지점 간 거리 계산 (Haversine formula)
     * @return 거리 (km)
     */
    private double calculateDistance(double lat1, double lng1, Double lat2, Double lng2) {
        if (lat2 == null || lng2 == null) {
            return Double.MAX_VALUE;
        }
        
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
     * 통계 정보 조회
     */
    public ShelterStatisticsDto getShelterStatistics() {
        long totalCount = shelterInfoRepository.count();
        
        // 보호소 구분별 통계
        List<Object[]> divisionStats = shelterInfoRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                ShelterInfo::getDivisionNm,
                Collectors.counting()
            ))
            .entrySet().stream()
            .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
            .collect(Collectors.toList());
        
        return ShelterStatisticsDto.builder()
            .totalCount(totalCount)
            .divisionStats(divisionStats)
            .build();
    }
    
    /**
     * 보호소 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ShelterStatisticsDto {
        private long totalCount;
        private List<Object[]> divisionStats;
    }
}