package com.petlogue.duopetbackend.info.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ShelterDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long shelterId;
        private Long userId;
        private String name;
        private String shelterName;
        private String phone;
        private String email;
        private String address;
        private String website;
        private Integer capacity;
        private String operatingHours;
        private String renameFilename;
        private String originalFilename;
        private String managerName;
        private String role;
        private String status;
        private Integer currentAnimals;
        private Double rating;
        private String type; // public, private, organization
        private Double distance; // 거리 (km)
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // 추가 프론트엔드 호환 필드들
        private List<String> facilities; // 보유 시설
        private List<String> adoptionProcess; // 입양 절차
        private List<String> specialNeeds; // 특별 서비스
        private String description; // 설명
        private BigDecimal latitude; // 위도 (지오코딩용)
        private BigDecimal longitude; // 경도 (지오코딩용)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String shelterName;
        private String phone;
        private String email;
        private String address;
        private String website;
        private Integer capacity;
        private String operatingHours;
        private String managerName;
        private String type;
        private Double rating;
        private Integer currentAnimals;
        private String description;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        private String keyword;
        private String address;
        private String type; // public, private, organization
        private String managerName;
        private Integer minCapacity;
        private Integer maxCapacity;
        private Double minRating;
        private BigDecimal userLatitude;
        private BigDecimal userLongitude;
        private Integer maxDistance; // km
        private String sortBy; // distance, rating, name, capacity
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
        private Long shelterId;
        private String name;
        private String address;
        private String phone;
        private String type;
        private Integer capacity;
        private Integer currentAnimals;
        private Double rating;
        private String operatingHours;
        private Double distance;
        private String managerName;
    }
}