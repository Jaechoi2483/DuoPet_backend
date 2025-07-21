package com.petlogue.duopetbackend.health.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class PetHealthScheduleDto {

    @Getter
    @Setter
    public static class CreateRequest {
        private Long petId;
        private String scheduleType;
        private String title;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate scheduleDate;
        private String scheduleTime;
        private String memo;
    }

    @Getter
    @Builder
    public static class Response {
        private Long scheduleId;
        private String scheduleType;
        private String title;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate scheduleDate;
        private String scheduleTime;
        private String memo;
    }
}
