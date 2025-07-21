package com.petlogue.duopetbackend.health.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class PetWeightDto {

    @Getter
    @Setter
    public static class CreateRequest {
        private Long petId;
        private BigDecimal weightKg;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate measuredDate;
        private String memo;
    }

    @Getter
    @Builder
    public static class Response {
        private Long weightId;
        private BigDecimal weightKg;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate measuredDate;
        private String memo;
    }
}
