package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomStatusUpdateDto {
    
    private String username;
    private String status; // JOINED, LEFT, CONSULTATION_STARTED, CONSULTATION_ENDED
    private String message;
    private LocalDateTime timestamp;
}