package com.petlogue.duopetbackend.health.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PetMedicalVisitDto {

    @Getter
    @Setter
    public static class CreateRequest {
        private Long petId;
        private String hospitalName;
        private String veterinarian;
        private LocalDate visitDate;
        private String visitReason;
        private String diagnosis;
        private String treatment;
        private BigDecimal cost;
    }

    @Getter
    @Builder
    public static class Response {
        private Long visitId;
        private String hospitalName;
        private String veterinarian;
        private LocalDate visitDate;
        private String visitReason;
        private String diagnosis;
        private String treatment;
        private BigDecimal cost;
    }
}
