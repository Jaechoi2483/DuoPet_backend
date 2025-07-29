package com.petlogue.duopetbackend.consultation.jpa.repository;

import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRoomRepository extends JpaRepository<ConsultationRoom, Long> {
    
    // UUID로 상담방 조회
    Optional<ConsultationRoom> findByRoomUuid(String roomUuid);
    
    // 사용자의 상담 목록 조회 (페이징)
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "JOIN FETCH cr.vet v " +
           "JOIN FETCH v.user vu " +
           "LEFT JOIN FETCH cr.pet p " +
           "WHERE cr.user.userId = :userId " +
           "ORDER BY cr.createdAt DESC")
    Page<ConsultationRoom> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // 수의사의 상담 목록 조회 (페이징)
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "JOIN FETCH cr.user u " +
           "LEFT JOIN FETCH cr.pet p " +
           "WHERE cr.vet.vetId = :vetId " +
           "ORDER BY cr.createdAt DESC")
    Page<ConsultationRoom> findByVetId(@Param("vetId") Long vetId, Pageable pageable);
    
    // 진행 중인 상담방 조회
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "WHERE cr.roomStatus = 'IN_PROGRESS' " +
           "AND (cr.user.userId = :userId OR cr.vet.user.userId = :userId)")
    List<ConsultationRoom> findActiveRoomsByUserId(@Param("userId") Long userId);
    
    // 상태별 상담 목록 조회
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "WHERE cr.user.userId = :userId " +
           "AND cr.roomStatus = :status " +
           "ORDER BY cr.createdAt DESC")
    List<ConsultationRoom> findByUserIdAndStatus(@Param("userId") Long userId, 
                                                @Param("status") String status);
    
    // 예약된 상담 중 시작 시간이 임박한 것들 조회
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "WHERE cr.roomStatus IN ('CREATED', 'WAITING') " +
           "AND cr.scheduledDatetime BETWEEN :now AND :until " +
           "ORDER BY cr.scheduledDatetime")
    List<ConsultationRoom> findUpcomingConsultations(@Param("now") LocalDateTime now,
                                                     @Param("until") LocalDateTime until);
    
    // 상담방 상태 업데이트
    @Modifying
    @Query("UPDATE ConsultationRoom cr " +
           "SET cr.roomStatus = :status, " +
           "cr.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE cr.roomId = :roomId")
    void updateRoomStatus(@Param("roomId") Long roomId, 
                         @Param("status") String status);
    
    @Query("SELECT cr FROM ConsultationRoom cr WHERE cr.roomStatus = 'WAITING' AND cr.createdAt < :threshold")
    List<ConsultationRoom> findWaitingRoomsCreatedBefore(@Param("threshold") LocalDateTime threshold);

    // 상담 시작 처리
    @Modifying
    @Query("UPDATE ConsultationRoom cr " +
           "SET cr.roomStatus = 'IN_PROGRESS', " +
           "cr.startedAt = :startTime, " +
           "cr.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE cr.roomId = :roomId")
    void startConsultation(@Param("roomId") Long roomId, 
                          @Param("startTime") LocalDateTime startTime);
    
    // 상담 종료 처리
    @Modifying
    @Query("UPDATE ConsultationRoom cr " +
           "SET cr.roomStatus = 'COMPLETED', " +
           "cr.endedAt = :endTime, " +
           "cr.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE cr.roomId = :roomId")
    void endConsultation(@Param("roomId") Long roomId, 
                        @Param("endTime") LocalDateTime endTime);
    
    // 결제 완료 처리
    @Modifying
    @Query("UPDATE ConsultationRoom cr " +
           "SET cr.paymentStatus = 'PAID', " +
           "cr.paymentMethod = :method, " +
           "cr.paidAt = :paidAt, " +
           "cr.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE cr.roomId = :roomId")
    void markAsPaid(@Param("roomId") Long roomId,
                   @Param("method") String method,
                   @Param("paidAt") LocalDateTime paidAt);
    
    // 완료된 상담 중 리뷰 없는 것들
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "LEFT JOIN cr.review r " +
           "WHERE cr.user.userId = :userId " +
           "AND cr.roomStatus = 'COMPLETED' " +
           "AND r IS NULL " +
           "ORDER BY cr.endedAt DESC")
    List<ConsultationRoom> findUnreviewedConsultations(@Param("userId") Long userId);
    
    // 통계: 수의사별 상담 수
    @Query("SELECT cr.vet.vetId, COUNT(cr) " +
           "FROM ConsultationRoom cr " +
           "WHERE cr.roomStatus = 'COMPLETED' " +
           "AND cr.endedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY cr.vet.vetId")
    List<Object[]> countConsultationsByVet(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    // 오늘의 예약 상담 (수의사용)
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "JOIN FETCH cr.user u " +
           "LEFT JOIN FETCH cr.pet p " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.scheduledDatetime >= :todayStart " +
           "AND cr.scheduledDatetime < :todayEnd " +
           "AND cr.roomStatus IN ('CREATED', 'WAITING') " +
           "ORDER BY cr.scheduledDatetime")
    List<ConsultationRoom> findTodayScheduledConsultations(@Param("vetId") Long vetId,
                                                          @Param("todayStart") LocalDateTime todayStart,
                                                          @Param("todayEnd") LocalDateTime todayEnd);
    
    // 수의사가 현재 진행 중인 상담이 있는지 확인
    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END " +
           "FROM ConsultationRoom cr " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.roomStatus = 'IN_PROGRESS'")
    boolean existsByVetIdAndInProgress(@Param("vetId") Long vetId);
    
    // Q&A 상담 관련 메서드 추가
    // 사용자별 Q&A 상담 목록 조회 (페이징)
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "JOIN FETCH cr.vet v " +
           "JOIN FETCH v.user vu " +
           "LEFT JOIN FETCH cr.pet p " +
           "WHERE cr.user.userId = :userId " +
           "AND cr.consultationType = :consultationType " +
           "ORDER BY cr.createdAt DESC")
    Page<ConsultationRoom> findByUserIdAndConsultationType(@Param("userId") Long userId, 
                                                           @Param("consultationType") String consultationType, 
                                                           Pageable pageable);
    
    // 수의사별 Q&A 상담 목록 조회 (페이징)
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "JOIN FETCH cr.user u " +
           "LEFT JOIN FETCH cr.pet p " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.consultationType = :consultationType " +
           "ORDER BY cr.createdAt DESC")
    Page<ConsultationRoom> findByVetIdAndConsultationType(@Param("vetId") Long vetId, 
                                                         @Param("consultationType") String consultationType, 
                                                         Pageable pageable);
    
    // 수의사별 특정 상태의 Q&A 상담 목록 조회 (페이징)
    @Query("SELECT cr FROM ConsultationRoom cr " +
           "JOIN FETCH cr.user u " +
           "LEFT JOIN FETCH cr.pet p " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.consultationType = :consultationType " +
           "AND cr.roomStatus = :roomStatus " +
           "ORDER BY cr.createdAt DESC")
    Page<ConsultationRoom> findByVetIdAndConsultationTypeAndRoomStatus(@Param("vetId") Long vetId, 
                                                                       @Param("consultationType") String consultationType, 
                                                                       @Param("roomStatus") String roomStatus, 
                                                                       Pageable pageable);
    
    // 미답변 Q&A 상담 수 조회
    @Query("SELECT COUNT(cr) FROM ConsultationRoom cr " +
           "WHERE cr.vet.vetId = :vetId " +
           "AND cr.consultationType = 'QNA' " +
           "AND cr.roomStatus = 'CREATED'")
    Long countPendingQnaByVetId(@Param("vetId") Long vetId);
}