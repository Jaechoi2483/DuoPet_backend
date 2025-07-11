package com.petlogue.duopetbackend.health.dto;

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
        private LocalDate scheduleDate;
        private String scheduleTime;
        private String memo;
    }
}
