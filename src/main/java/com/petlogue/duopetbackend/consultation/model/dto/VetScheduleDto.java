package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetScheduleDto {
    
    private Long scheduleId;
    private Long vetId;
    private String vetName;
    private LocalDate scheduleDate;
    private String startTime;
    private String endTime;
    private String slotStatus; // Y/N for availability
    private Integer maxConsultations;
    private Integer currentBookings;
    private LocalDateTime createdAt;
    
    // For display
    public String getTimeSlot() {
        return String.format("%s - %s", startTime, endTime);
    }
    
    public boolean isAvailable() {
        return "Y".equals(slotStatus) && currentBookings < maxConsultations;
    }
}