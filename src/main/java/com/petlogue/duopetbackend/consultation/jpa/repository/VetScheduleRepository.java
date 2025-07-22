package com.petlogue.duopetbackend.consultation.jpa.repository;

import com.petlogue.duopetbackend.consultation.jpa.entity.VetSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VetScheduleRepository extends JpaRepository<VetSchedule, Long> {
    
    // 특정 수의사의 특정 날짜 스케줄 조회
    @Query("SELECT vs FROM VetSchedule vs " +
           "WHERE vs.vet.vetId = :vetId " +
           "AND vs.scheduleDate = :date " +
           "ORDER BY vs.startTime")
    List<VetSchedule> findByVetAndDate(@Param("vetId") Long vetId, 
                                      @Param("date") LocalDate date);
    
    // 특정 수의사의 기간별 스케줄 조회
    @Query("SELECT vs FROM VetSchedule vs " +
           "WHERE vs.vet.vetId = :vetId " +
           "AND vs.scheduleDate BETWEEN :startDate AND :endDate " +
           "ORDER BY vs.scheduleDate, vs.startTime")
    List<VetSchedule> findByVetAndDateRange(@Param("vetId") Long vetId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    // 예약 가능한 스케줄만 조회
    @Query("SELECT vs FROM VetSchedule vs " +
           "WHERE vs.vet.vetId = :vetId " +
           "AND vs.scheduleDate >= :fromDate " +
           "AND vs.isAvailable = 'Y' " +
           "AND vs.currentBookings < vs.maxConsultations " +
           "ORDER BY vs.scheduleDate, vs.startTime")
    List<VetSchedule> findAvailableSchedules(@Param("vetId") Long vetId,
                                            @Param("fromDate") LocalDate fromDate);
    
    // 예약 가능한 스케줄 기간 조회
    @Query("SELECT vs FROM VetSchedule vs " +
           "WHERE vs.vet.vetId = :vetId " +
           "AND vs.scheduleDate BETWEEN :startDate AND :endDate " +
           "AND vs.isAvailable = 'Y' " +
           "AND vs.currentBookings < vs.maxConsultations " +
           "ORDER BY vs.scheduleDate, vs.startTime")
    List<VetSchedule> findAvailableSchedules(@Param("vetId") Long vetId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    
    // 특정 날짜의 모든 수의사 스케줄 조회 (관리자용)
    @Query("SELECT vs FROM VetSchedule vs " +
           "JOIN FETCH vs.vet v " +
           "WHERE vs.scheduleDate = :date " +
           "ORDER BY v.vetId, vs.startTime")
    List<VetSchedule> findAllByDate(@Param("date") LocalDate date);
    
    // 예약 수 증가
    @Modifying
    @Query("UPDATE VetSchedule vs " +
           "SET vs.currentBookings = vs.currentBookings + 1, " +
           "vs.isAvailable = CASE " +
           "  WHEN vs.currentBookings + 1 >= vs.maxConsultations THEN 'N' " +
           "  ELSE vs.isAvailable END " +
           "WHERE vs.scheduleId = :scheduleId " +
           "AND vs.currentBookings < vs.maxConsultations")
    int incrementBooking(@Param("scheduleId") Long scheduleId);
    
    // 예약 수 감소
    @Modifying
    @Query("UPDATE VetSchedule vs " +
           "SET vs.currentBookings = vs.currentBookings - 1, " +
           "vs.isAvailable = 'Y' " +
           "WHERE vs.scheduleId = :scheduleId " +
           "AND vs.currentBookings > 0")
    int decrementBooking(@Param("scheduleId") Long scheduleId);
    
    // 지난 스케줄 삭제 (배치 작업용)
    @Modifying
    @Query("DELETE FROM VetSchedule vs " +
           "WHERE vs.scheduleDate < :beforeDate")
    int deleteOldSchedules(@Param("beforeDate") LocalDate beforeDate);
    
    // 특정 시간대 중복 체크
    @Query("SELECT COUNT(vs) > 0 FROM VetSchedule vs " +
           "WHERE vs.vet.vetId = :vetId " +
           "AND vs.scheduleDate = :date " +
           "AND ((vs.startTime <= :startTime AND vs.endTime > :startTime) " +
           "  OR (vs.startTime < :endTime AND vs.endTime >= :endTime) " +
           "  OR (vs.startTime >= :startTime AND vs.endTime <= :endTime))")
    boolean existsOverlappingSchedule(@Param("vetId") Long vetId,
                                     @Param("date") LocalDate date,
                                     @Param("startTime") String startTime,
                                     @Param("endTime") String endTime);
    
    // 특정 수의사의 다음 가능한 스케줄
    @Query("SELECT vs FROM VetSchedule vs " +
           "WHERE vs.vet.vetId = :vetId " +
           "AND vs.scheduleDate >= CURRENT_DATE " +
           "AND vs.isAvailable = 'Y' " +
           "AND vs.currentBookings < vs.maxConsultations " +
           "ORDER BY vs.scheduleDate, vs.startTime")
    Optional<VetSchedule> findNextAvailableSchedule(@Param("vetId") Long vetId);
    
    // 특정 시간 범위의 스케줄 상태 업데이트
    @Modifying
    @Query("UPDATE VetSchedule vs " +
           "SET vs.isAvailable = :status " +
           "WHERE vs.vet.vetId = :vetId " +
           "AND vs.scheduleDate = :date " +
           "AND vs.startTime >= :startTime " +
           "AND vs.endTime <= :endTime")
    void updateScheduleStatusByTimeRange(@Param("vetId") Long vetId,
                                        @Param("date") LocalDate date,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime,
                                        @Param("status") String status);
}