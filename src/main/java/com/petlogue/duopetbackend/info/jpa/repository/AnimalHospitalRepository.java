package com.petlogue.duopetbackend.info.jpa.repository;

import com.petlogue.duopetbackend.info.jpa.entity.AnimalHospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalHospitalRepository extends JpaRepository<AnimalHospital, Long> {
    
    // 관리번호로 조회 (중복 체크용)
    Optional<AnimalHospital> findByManagementNo(String managementNo);
    
    // 영업 중인 병원만 조회
    @Query("SELECT h FROM AnimalHospital h WHERE h.businessStatus = '영업/정상' AND h.detailedStatus = '정상'")
    Page<AnimalHospital> findOperatingHospitals(Pageable pageable);
    
    // 시도별 조회
    @Query("SELECT h FROM AnimalHospital h WHERE h.businessStatus = '영업/정상' AND h.detailedStatus = '정상' AND h.city = :city")
    Page<AnimalHospital> findByCity(@Param("city") String city, Pageable pageable);
    
    // 시군구별 조회
    @Query("SELECT h FROM AnimalHospital h WHERE h.businessStatus = '영업/정상' AND h.detailedStatus = '정상' AND h.city = :city AND h.district = :district")
    Page<AnimalHospital> findByCityAndDistrict(@Param("city") String city, @Param("district") String district, Pageable pageable);
    
    // 키워드 검색 (병원명, 주소)
    @Query("SELECT h FROM AnimalHospital h WHERE h.businessStatus = '영업/정상' AND h.detailedStatus = '정상' " +
           "AND (LOWER(h.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(h.roadAddress) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(h.jibunAddress) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AnimalHospital> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 근처 병원 찾기 (Haversine formula 사용)
    @Query(value = "SELECT h.*, " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(h.latitude)) * " +
           "cos(radians(h.longitude) - radians(:lon)) + " +
           "sin(radians(:lat)) * sin(radians(h.latitude)))) AS distance " +
           "FROM ANIMAL_HOSPITALS h " +
           "WHERE h.business_status = '영업/정상' AND h.detailed_status = '정상' " +
           "AND h.latitude IS NOT NULL AND h.longitude IS NOT NULL " +
           "HAVING distance < :radius " +
           "ORDER BY distance", 
           nativeQuery = true)
    List<AnimalHospital> findNearbyHospitals(@Param("lat") double latitude, 
                                             @Param("lon") double longitude, 
                                             @Param("radius") double radiusInKm);
    
    // 전화번호가 있는 영업 중인 병원 수
    @Query("SELECT COUNT(h) FROM AnimalHospital h WHERE h.businessStatus = '영업/정상' AND h.detailedStatus = '정상' AND h.phone IS NOT NULL AND h.phone != ''")
    long countOperatingHospitalsWithPhone();
    
    // 좌표가 있는 영업 중인 병원 수
    @Query("SELECT COUNT(h) FROM AnimalHospital h WHERE h.businessStatus = '영업/정상' AND h.detailedStatus = '정상' AND h.latitude IS NOT NULL AND h.longitude IS NOT NULL")
    long countOperatingHospitalsWithCoordinates();
    
    // 시도별 병원 수 통계
    @Query("SELECT h.city, COUNT(h) FROM AnimalHospital h WHERE h.businessStatus = '영업/정상' AND h.detailedStatus = '정상' GROUP BY h.city ORDER BY COUNT(h) DESC")
    List<Object[]> countByCityGrouped();
}