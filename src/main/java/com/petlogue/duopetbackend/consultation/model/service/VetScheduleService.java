package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.model.dto.VetScheduleDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetSchedule;
import com.petlogue.duopetbackend.consultation.jpa.repository.VetScheduleRepository;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import com.petlogue.duopetbackend.user.jpa.repository.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VetScheduleService {
    
    private final VetScheduleRepository vetScheduleRepository;
    private final VetRepository vetRepository;
    
    /**
     * 수의사 스케줄 생성 (일괄)
     */
    @Transactional
    public List<VetSchedule> createScheduleSlots(Long vetId, LocalDate date, 
                                                  LocalTime startTime, LocalTime endTime, 
                                                  int slotDurationMinutes) {
        VetEntity vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        
        List<VetSchedule> schedules = new ArrayList<>();
        LocalDateTime currentSlot = LocalDateTime.of(date, startTime);
        LocalDateTime lastSlot = LocalDateTime.of(date, endTime);
        
        while (currentSlot.isBefore(lastSlot)) {
            VetSchedule schedule = VetSchedule.builder()
                    .vet(vet)
                    .scheduleDate(currentSlot.toLocalDate())
                    .startTime(String.format("%02d:%02d", currentSlot.getHour(), currentSlot.getMinute()))
                    .endTime(String.format("%02d:%02d", 
                            currentSlot.plusMinutes(slotDurationMinutes).getHour(),
                            currentSlot.plusMinutes(slotDurationMinutes).getMinute()))
                    .isAvailable("Y")
                    .build();
            
            schedules.add(schedule);
            currentSlot = currentSlot.plusMinutes(slotDurationMinutes);
        }
        
        List<VetSchedule> savedSchedules = vetScheduleRepository.saveAll(schedules);
        log.info("Created {} schedule slots for vet {} on {}", 
                savedSchedules.size(), vetId, date);
        
        return savedSchedules;
    }
    
    /**
     * 특정 날짜의 수의사 스케줄 조회
     */
    public List<VetScheduleDto> getVetScheduleByDate(Long vetId, LocalDate date) {
        List<VetSchedule> schedules = vetScheduleRepository.findByVetAndDate(vetId, date);
        
        return schedules.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 예약 가능한 스케줄 조회
     */
    public List<VetScheduleDto> getAvailableSchedules(Long vetId, LocalDate startDate, LocalDate endDate) {
        List<VetSchedule> schedules = vetScheduleRepository
                .findAvailableSchedules(vetId, startDate, endDate);
        
        return schedules.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 스케줄 상태 업데이트
     */
    @Transactional
    public void updateScheduleStatus(Long scheduleId, String status) {
        VetSchedule schedule = vetScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        
        schedule.setIsAvailable(status);
        vetScheduleRepository.save(schedule);
        
        log.info("Updated schedule {} availability to {}", scheduleId, status);
    }
    
    /**
     * 스케줄 블록 처리 (휴무, 점심시간 등)
     */
    @Transactional
    public void blockScheduleSlots(Long vetId, LocalDate date, 
                                   LocalTime startTime, LocalTime endTime, String reason) {
        vetScheduleRepository.updateScheduleStatusByTimeRange(
                vetId, date, 
                String.format("%02d:%02d", startTime.getHour(), startTime.getMinute()),
                String.format("%02d:%02d", endTime.getHour(), endTime.getMinute()), 
                "N");
        
        log.info("Blocked schedule slots for vet {} on {} from {} to {}: {}", 
                vetId, date, startTime, endTime, reason);
    }
    
    /**
     * 스케줄 삭제
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        VetSchedule schedule = vetScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        
        if (schedule.getCurrentBookings() > 0) {
            throw new IllegalStateException("Cannot delete schedule with existing bookings");
        }
        
        vetScheduleRepository.delete(schedule);
        log.info("Deleted schedule {}", scheduleId);
    }
    
    /**
     * 이번 주 스케줄 조회
     */
    public List<VetScheduleDto> getThisWeekSchedule(Long vetId) {
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        List<VetSchedule> schedules = vetScheduleRepository
                .findAvailableSchedules(vetId, startOfWeek, endOfWeek);
        
        return schedules.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * VetSchedule을 DTO로 변환
     */
    private VetScheduleDto toDto(VetSchedule schedule) {
        return VetScheduleDto.builder()
                .scheduleId(schedule.getScheduleId())
                .vetId(schedule.getVet().getVetId())
                .vetName(schedule.getVet().getUser().getNickname())
                .scheduleDate(schedule.getScheduleDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .slotStatus(schedule.getIsAvailable())
                .maxConsultations(schedule.getMaxConsultations())
                .currentBookings(schedule.getCurrentBookings())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}