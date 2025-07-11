package com.petlogue.duopetbackend.health.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class PetVaccinDto {

    @Getter
    @Setter
    public static class CreateRequest {
        private Long petId;
        private String vaccineName;
        private LocalDate scheduledDate;
        private String description;
        private LocalDate administeredDate;
    }

    @Getter
    @Builder
    public static class Response {
        private Long vaccinationId;
        private String vaccineName;
        private LocalDate scheduledDate;
        private String description;
        private LocalDate administeredDate;
    }
}
