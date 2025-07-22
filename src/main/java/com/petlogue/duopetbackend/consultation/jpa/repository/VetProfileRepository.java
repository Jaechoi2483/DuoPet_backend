package com.petlogue.duopetbackend.consultation.jpa.repository;

import com.petlogue.duopetbackend.consultation.jpa.entity.VetProfile;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VetProfileRepository extends JpaRepository<VetProfile, Long> {
    
    // VetEntity의 vetId로 프로필 조회
    Optional<VetProfile> findByVet_VetId(Long vetId);
    
    // VetEntity의 vetId로 프로필 존재 여부 확인
    boolean existsByVet_VetId(Long vetId);
    
    // 온라인 상태이고 상담 가능한 수의사 목록
    @Query("SELECT vp FROM VetProfile vp " +
           "JOIN FETCH vp.vet v " +
           "JOIN FETCH v.user u " +
           "WHERE vp.isOnline = 'Y' " +
           "AND vp.isAvailable = 'Y' " +
           "ORDER BY vp.ratingAvg DESC")
    List<VetProfile> findOnlineAndAvailableVets();
    
    // 모든 상담 가능한 수의사 목록 (온/오프라인 포함)
    @Query("SELECT vp FROM VetProfile vp " +
           "JOIN FETCH vp.vet v " +
           "JOIN FETCH v.user u " +
           "WHERE vp.isAvailable = 'Y' " +
           "ORDER BY vp.isOnline DESC, vp.ratingAvg DESC")
    List<VetProfile> findAllAvailableVets();
    
    // 전문 분야로 검색
    @Query("SELECT vp FROM VetProfile vp " +
           "JOIN FETCH vp.vet v " +
           "JOIN FETCH v.user u " +
           "WHERE vp.isAvailable = 'Y' " +
           "AND LOWER(v.specialization) LIKE LOWER(CONCAT('%', :specialty, '%')) " +
           "ORDER BY vp.ratingAvg DESC")
    List<VetProfile> findBySpecialty(@Param("specialty") String specialty);
    
    // 평점 범위로 검색
    @Query("SELECT vp FROM VetProfile vp " +
           "WHERE vp.isAvailable = 'Y' " +
           "AND vp.ratingAvg >= :minRating " +
           "ORDER BY vp.ratingAvg DESC")
    List<VetProfile> findByMinimumRating(@Param("minRating") BigDecimal minRating);
    
    // 온라인 상태 업데이트
    @Modifying
    @Query("UPDATE VetProfile vp " +
           "SET vp.isOnline = :status, " +
           "vp.lastOnlineAt = CASE WHEN :status = 'Y' THEN :now ELSE vp.lastOnlineAt END " +
           "WHERE vp.vet.vetId = :vetId")
    void updateOnlineStatus(@Param("vetId") Long vetId, 
                           @Param("status") String status, 
                           @Param("now") LocalDateTime now);
    
    // 상담 통계 업데이트
    @Modifying
    @Query("UPDATE VetProfile vp " +
           "SET vp.consultationCount = vp.consultationCount + 1 " +
           "WHERE vp.vet.vetId = :vetId")
    void incrementConsultationCount(@Param("vetId") Long vetId);
    
    // 평점 업데이트 (평균 계산)
    @Modifying
    @Query("UPDATE VetProfile vp " +
           "SET vp.ratingAvg = " +
           "(SELECT AVG(cr.rating) FROM ConsultationReview cr " +
           " WHERE cr.vet.vetId = :vetId AND cr.isVisible = 'Y'), " +
           "vp.ratingCount = " +
           "(SELECT COUNT(cr) FROM ConsultationReview cr " +
           " WHERE cr.vet.vetId = :vetId AND cr.isVisible = 'Y') " +
           "WHERE vp.vet.vetId = :vetId")
    void updateRatingStatistics(@Param("vetId") Long vetId);
    
    // 상담료 범위로 검색
    @Query("SELECT vp FROM VetProfile vp " +
           "WHERE vp.isAvailable = 'Y' " +
           "AND vp.consultationFee BETWEEN :minFee AND :maxFee " +
           "ORDER BY vp.consultationFee ASC")
    List<VetProfile> findByFeeRange(@Param("minFee") BigDecimal minFee, 
                                   @Param("maxFee") BigDecimal maxFee);
    
    // 가장 인기 있는 수의사 TOP N
    @Query(value = "SELECT vp FROM VetProfile vp " +
                   "WHERE vp.isAvailable = 'Y' " +
                   "ORDER BY vp.consultationCount DESC, vp.ratingAvg DESC")
    List<VetProfile> findTopVets(org.springframework.data.domain.Pageable pageable);
    
    // 키워드로 수의사 검색 (이름, 전문분야)
    @Query("SELECT vp FROM VetProfile vp " +
           "JOIN FETCH vp.vet v " +
           "JOIN FETCH v.user u " +
           "WHERE vp.isAvailable = 'Y' " +
           "AND (LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(v.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<VetProfile> searchByKeyword(@Param("keyword") String keyword);
    
    // 평점 높은 순으로 조회
    @Query("SELECT vp FROM VetProfile vp " +
           "WHERE vp.isAvailable = 'Y' " +
           "ORDER BY vp.ratingAvg DESC")
    List<VetProfile> findTopRatedVets(org.springframework.data.domain.Pageable pageable);
    
    // 상담 가능 상태 업데이트
    @Modifying
    @Query("UPDATE VetProfile vp " +
           "SET vp.isAvailable = :status " +
           "WHERE vp.vet.vetId = :vetId")
    void updateAvailability(@Param("vetId") Long vetId, @Param("status") String status);
    
    // VetEntity로 프로필 존재 여부 확인
    boolean existsByVet(VetEntity vet);
}