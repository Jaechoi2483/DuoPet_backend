package com.petlogue.duopetbackend.health.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PetWeightDto {

    @Getter
    @Setter
    public static class CreateRequest {
        private Long petId;
        private BigDecimal weightKg;
        private LocalDate measuredDate;
        private String memo;
    }

    @Getter
    @Builder
    public static class Response {
        private Long weightId;
        private BigDecimal weightKg;
        private LocalDate measuredDate;
        private String memo;
    }
}
