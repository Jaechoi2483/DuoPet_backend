package com.petlogue.duopetbackend.health.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class PetVaccinDto {

    @Getter
    @Setter
    public static class CreateRequest {
        private Long petId;
        private String vaccineName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate scheduledDate;
        private String description;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate administeredDate;
        private String hospitalName;
    }

    @Getter
    @Builder
    public static class Response {
        private Long vaccinationId;
        private String vaccineName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate scheduledDate;
        private String description;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate administeredDate;
        private String hospitalName;
    }
}
