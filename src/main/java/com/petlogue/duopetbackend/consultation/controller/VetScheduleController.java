package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.consultation.model.dto.ApiResponse;
import com.petlogue.duopetbackend.consultation.model.dto.VetScheduleDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetSchedule;
import com.petlogue.duopetbackend.consultation.model.service.VetScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/consultation/schedules")
@RequiredArgsConstructor
public class VetScheduleController {
    
    private final VetScheduleService vetScheduleService;
    
    /**
     * 수의사 스케줄 일괄 생성
     * POST /api/consultation/schedules/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<List<VetSchedule>>> createScheduleSlots(
            @Valid @RequestBody CreateScheduleBatchDto dto) {
        
        try {
            List<VetSchedule> schedules = vetScheduleService.createScheduleSlots(
                    dto.getVetId(),
                    dto.getDate(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getSlotDurationMinutes()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            String.format("%d개의 스케줄 슬롯이 생성되었습니다.", schedules.size()), 
                            schedules));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 특정 날짜의 수의사 스케줄 조회
     * GET /api/consultation/schedules/vet/{vetId}/date/{date}
     */
    @GetMapping("/vet/{vetId}/date/{date}")
    public ResponseEntity<ApiResponse<List<VetScheduleDto>>> getScheduleByDate(
            @PathVariable Long vetId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<VetScheduleDto> schedules = vetScheduleService.getVetScheduleByDate(vetId, date);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }
    
    /**
     * 예약 가능한 스케줄 조회
     * GET /api/consultation/schedules/vet/{vetId}/available
     */
    @GetMapping("/vet/{vetId}/available")
    public ResponseEntity<ApiResponse<List<VetScheduleDto>>> getAvailableSchedules(
            @PathVariable Long vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("시작일이 종료일보다 늦을 수 없습니다."));
        }
        
        List<VetScheduleDto> schedules = vetScheduleService.getAvailableSchedules(vetId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }
    
    /**
     * 이번 주 스케줄 조회
     * GET /api/consultation/schedules/vet/{vetId}/this-week
     */
    @GetMapping("/vet/{vetId}/this-week")
    public ResponseEntity<ApiResponse<List<VetScheduleDto>>> getThisWeekSchedule(
            @PathVariable Long vetId) {
        
        List<VetScheduleDto> schedules = vetScheduleService.getThisWeekSchedule(vetId);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }
    
    /**
     * 스케줄 상태 업데이트
     * PATCH /api/consultation/schedules/{scheduleId}/status
     */
    @PatchMapping("/{scheduleId}/status")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> updateScheduleStatus(
            @PathVariable Long scheduleId,
            @RequestParam String status) {
        
        try {
            vetScheduleService.updateScheduleStatus(scheduleId, status);
            return ResponseEntity.ok(ApiResponse.success("스케줄 상태가 업데이트되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 스케줄 블록 처리 (휴무, 점심시간 등)
     * POST /api/consultation/schedules/block
     */
    @PostMapping("/block")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> blockScheduleSlots(
            @Valid @RequestBody BlockScheduleDto dto) {
        
        try {
            vetScheduleService.blockScheduleSlots(
                    dto.getVetId(),
                    dto.getDate(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getReason()
            );
            
            return ResponseEntity.ok(ApiResponse.success("스케줄이 블록 처리되었습니다.", null));
            
        } catch (Exception e) {
            log.error("Error blocking schedule slots", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("스케줄 블록 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 스케줄 삭제
     * DELETE /api/consultation/schedules/{scheduleId}
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable Long scheduleId) {
        
        try {
            vetScheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(ApiResponse.success("스케줄이 삭제되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // DTO Classes
    @Data
    public static class CreateScheduleBatchDto {
        @NotNull(message = "수의사 ID는 필수입니다")
        private Long vetId;
        
        @NotNull(message = "날짜는 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;
        
        @NotNull(message = "시작 시간은 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime startTime;
        
        @NotNull(message = "종료 시간은 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime endTime;
        
        @NotNull(message = "슬롯 길이는 필수입니다")
        private Integer slotDurationMinutes;
    }
    
    @Data
    public static class BlockScheduleDto {
        @NotNull(message = "수의사 ID는 필수입니다")
        private Long vetId;
        
        @NotNull(message = "날짜는 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;
        
        @NotNull(message = "시작 시간은 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime startTime;
        
        @NotNull(message = "종료 시간은 필수입니다")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime endTime;
        
        @NotNull(message = "사유는 필수입니다")
        private String reason;
    }
}