package com.petlogue.duopetbackend.consultation.jpa.repository;

import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationReviewRepository extends JpaRepository<ConsultationReview, Long> {
    
    // 상담방 ID로 리뷰 조회
    Optional<ConsultationReview> findByConsultationRoom_RoomId(Long roomId);
    
    // 수의사별 리뷰 목록 조회 (공개된 것만, 페이징)
    @Query("SELECT cr FROM ConsultationReview cr " +
           "JOIN FETCH cr.user u " +
           "JOIN FETCH cr.consultationRoom room " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.isVisible = 'Y' " +
           "ORDER BY cr.createdAt DESC")
    Page<ConsultationReview> findByVetId(@Param("vetId") Long vetId, Pageable pageable);
    
    // 사용자가 작성한 리뷰 목록
    @Query("SELECT cr FROM ConsultationReview cr " +
           "JOIN FETCH cr.vet v " +
           "JOIN FETCH v.user vu " +
           "JOIN FETCH cr.consultationRoom room " +
           "WHERE cr.user.userId = :userId " +
           "ORDER BY cr.createdAt DESC")
    List<ConsultationReview> findByUserId(@Param("userId") Long userId);
    
    // 수의사별 평균 평점 계산
    @Query("SELECT AVG(cr.rating) FROM ConsultationReview cr " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.isVisible = 'Y'")
    Double calculateAverageRating(@Param("vetId") Long vetId);
    
    // 수의사별 세부 평점 통계
    @Query("SELECT " +
           "AVG(cr.rating) as avgRating, " +
           "AVG(cr.kindnessScore) as avgKindness, " +
           "AVG(cr.professionalScore) as avgProfessional, " +
           "AVG(cr.responseScore) as avgResponse, " +
           "COUNT(cr) as totalCount " +
           "FROM ConsultationReview cr " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.isVisible = 'Y'")
    Object[] calculateDetailedStatistics(@Param("vetId") Long vetId);
    
    // 최근 리뷰 N개 조회 (홈페이지 표시용)
    @Query("SELECT cr FROM ConsultationReview cr " +
           "JOIN FETCH cr.user u " +
           "JOIN FETCH cr.vet v " +
           "JOIN FETCH v.user vu " +
           "WHERE cr.isVisible = 'Y' " +
           "AND cr.rating >= 4 " +
           "ORDER BY cr.createdAt DESC")
    List<ConsultationReview> findRecentHighRatedReviews(Pageable pageable);
    
    // 평점별 리뷰 수 집계
    @Query("SELECT cr.rating, COUNT(cr) " +
           "FROM ConsultationReview cr " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.isVisible = 'Y' " +
           "GROUP BY cr.rating " +
           "ORDER BY cr.rating DESC")
    List<Object[]> countReviewsByRating(@Param("vetId") Long vetId);
    
    // 답변이 없는 리뷰 조회 (수의사용)
    @Query("SELECT cr FROM ConsultationReview cr " +
           "JOIN FETCH cr.user u " +
           "JOIN FETCH cr.consultationRoom room " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.vetReply IS NULL " +
           "AND cr.isVisible = 'Y' " +
           "ORDER BY cr.createdAt DESC")
    List<ConsultationReview> findUnansweredReviews(@Param("vetId") Long vetId);
    
    // 키워드로 리뷰 검색
    @Query("SELECT cr FROM ConsultationReview cr " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.isVisible = 'Y' " +
           "AND (cr.reviewContent LIKE CONCAT('%', :keyword, '%') " +
           "  OR cr.vetReply LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY cr.createdAt DESC")
    List<ConsultationReview> searchReviews(@Param("vetId") Long vetId,
                                          @Param("keyword") String keyword);
    
    // 특정 평점 이상의 리뷰만 조회
    @Query("SELECT cr FROM ConsultationReview cr " +
           "JOIN FETCH cr.user u " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.isVisible = 'Y' " +
           "AND cr.rating >= :minRating " +
           "ORDER BY cr.rating DESC, cr.createdAt DESC")
    List<ConsultationReview> findByMinimumRating(@Param("vetId") Long vetId,
                                                 @Param("minRating") Integer minRating);
    
    // 수의사의 가시적인 리뷰 조회
    @Query("SELECT cr FROM ConsultationReview cr " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.isVisible = :isVisible")
    List<ConsultationReview> findByVetIdAndIsVisible(@Param("vetId") Long vetId, @Param("isVisible") String isVisible);
}