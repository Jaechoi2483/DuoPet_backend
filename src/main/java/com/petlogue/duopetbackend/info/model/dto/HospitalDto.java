package com.petlogue.duopetbackend.info.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class HospitalDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long vetId;
        private String name;
        private String licenseNumber;
        private String phone;
        private String email;
        private String address;
        private String website;
        private String specialization;
        private String renameFilename;
        private String originalFilename;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String openHours;
        private Boolean isEmergency;
        private List<String> services;
        private BigDecimal rating;
        private Integer reviewCount;
        private String description;
        private Double distance; // 거리 (km)
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String name;
        private String licenseNumber;
        private String phone;
        private String email;
        private String address;
        private String website;
        private String specialization;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String openHours;
        private Boolean isEmergency;
        private List<String> services;
        private BigDecimal rating;
        private Integer reviewCount;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        private String keyword;
        private String address;
        private String specialization;
        private String service;
        private Boolean isEmergency;
        private BigDecimal minRating;
        private BigDecimal userLatitude;
        private BigDecimal userLongitude;
        private Integer maxDistance; // km
        private String sortBy; // distance, rating, name
        private String sortOrder; // asc, desc
        private Integer page;
        private Integer size;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long vetId;
        private String name;
        private String address;
        private String phone;
        private String specialization;
        private Boolean isEmergency;
        private BigDecimal rating;
        private Integer reviewCount;
        private String openHours;
        private Double distance;
    }
}