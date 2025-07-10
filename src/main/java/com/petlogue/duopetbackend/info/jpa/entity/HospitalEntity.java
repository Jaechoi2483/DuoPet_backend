package com.petlogue.duopetbackend.info.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 50)
    private String loginId;

    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    @Column(name = "nickname", length = 50, nullable = false)
    private String hospitalName; // USERS.NICKNAME -> 병원 이름

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "user_email", length = 300, nullable = false)
    private String email;

    @Column(name = "role", length = 50, nullable = false)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "rename_filename", length = 255)
    private String renameFilename;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    // 추가 필드들 (실제 테이블에 없지만 프론트엔드에서 필요한 정보)
    @Transient
    private BigDecimal latitude;

    @Transient
    private BigDecimal longitude;

    @Transient
    private String openHours;

    @Transient
    private Boolean isEmergency;

    @Transient
    private String services;

    @Transient
    private BigDecimal rating;

    @Transient
    private Integer reviewCount;

    @Transient
    private String description;

    @Transient
    private String specialization;

    // 기본값 설정 메서드
    @PostLoad
    protected void setDefaults() {
        if (this.rating == null) {
            this.rating = new BigDecimal("4.5"); // 기본 평점
        }
        if (this.reviewCount == null) {
            this.reviewCount = 0; // 기본 리뷰 수
        }
        if (this.openHours == null) {
            this.openHours = "09:00 - 18:00"; // 기본 운영시간
        }
        if (this.isEmergency == null) {
            this.isEmergency = false; // 기본값: 응급병원 아님
        }
        if (this.services == null) {
            this.services = "진료,건강검진,예방접종"; // 기본 서비스
        }
        if (this.specialization == null) {
            this.specialization = "종합진료"; // 기본 전문분야
        }
        if (this.description == null) {
            this.description = "반려동물의 건강을 책임지는 " + this.hospitalName + "입니다.";
        }
    }
}