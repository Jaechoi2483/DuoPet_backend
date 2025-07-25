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
public class ConsultationNotificationDto {
    private String type; // NEW_CONSULTATION, STATUS_CHANGE, MESSAGE, etc.
    private Long roomId;
    private String roomUuid;
    private Long userId;
    private String userName;
    private String petName;
    private String chiefComplaint;
    private String consultationType;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}