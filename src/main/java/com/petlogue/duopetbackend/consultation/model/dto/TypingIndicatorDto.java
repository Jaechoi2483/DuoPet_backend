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
public class TypingIndicatorDto {
    
    private String username;
    private boolean isTyping;
    private LocalDateTime timestamp;
}