package com.petlogue.duopetbackend.info.jpa.repository;

import com.petlogue.duopetbackend.info.jpa.entity.ShelterInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 공공데이터 보호소 정보 Repository
 */
@Repository
public interface ShelterInfoRepository extends JpaRepository<ShelterInfo, Long> {
    
    // 관리번호로 조회 (중복 체크용)
    Optional<ShelterInfo> findByCareRegNo(String careRegNo);
    
    // 보호소명으로 검색 (부분 일치)
    Page<ShelterInfo> findByCareNmContaining(String careNm, Pageable pageable);
    
    // 관할기관별 조회
    Page<ShelterInfo> findByOrgNm(String orgNm, Pageable pageable);
    
    // 보호소 구분별 조회
    Page<ShelterInfo> findByDivisionNm(String divisionNm, Pageable pageable);
    
    // 지역별 조회 (주소 기반)
    @Query("SELECT s FROM ShelterInfo s WHERE s.careAddr LIKE %:region% OR s.jibunAddr LIKE %:region%")
    Page<ShelterInfo> findByRegion(@Param("region") String region, Pageable pageable);
    
    // 위치 기반 근처 보호소 조회 (거리 계산)
    @Query(value = "SELECT s.*, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * " +
            "cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(s.lat)))) AS distance " +
            "FROM SHELTER_INFO s " +
            "WHERE s.lat IS NOT NULL AND s.lng IS NOT NULL " +
            "HAVING distance <= :radius " +
            "ORDER BY distance", 
            nativeQuery = true)
    List<ShelterInfo> findNearbySheleters(
            @Param("lat") Double lat, 
            @Param("lng") Double lng, 
            @Param("radius") Double radius
    );
    
    // 동물 종류별 조회 (개/고양이)
    @Query("SELECT s FROM ShelterInfo s WHERE s.saveTrgtAnimal LIKE %:animal%")
    Page<ShelterInfo> findByAnimalType(@Param("animal") String animal, Pageable pageable);
    
    // 운영중인 보호소 조회
    @Query("SELECT s FROM ShelterInfo s WHERE s.weekOprStime IS NOT NULL AND s.weekOprEtime IS NOT NULL")
    Page<ShelterInfo> findOperatingShelters(Pageable pageable);
    
    // 전화번호로 조회 (기존 회원 보호소와 매칭용)
    Optional<ShelterInfo> findByCareTel(String careTel);
    
    // 복합 검색
    @Query("SELECT s FROM ShelterInfo s WHERE " +
            "(:keyword IS NULL OR s.careNm LIKE %:keyword% OR s.careAddr LIKE %:keyword%) AND " +
            "(:orgNm IS NULL OR s.orgNm = :orgNm) AND " +
            "(:divisionNm IS NULL OR s.divisionNm = :divisionNm) AND " +
            "(:animal IS NULL OR s.saveTrgtAnimal LIKE %:animal%)")
    Page<ShelterInfo> searchShelters(
            @Param("keyword") String keyword,
            @Param("orgNm") String orgNm,
            @Param("divisionNm") String divisionNm,
            @Param("animal") String animal,
            Pageable pageable
    );
    
    // 관리번호 리스트로 일괄 조회 (동기화시 사용)
    List<ShelterInfo> findByCareRegNoIn(List<String> careRegNos);
}