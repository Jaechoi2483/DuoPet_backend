package com.petlogue.duopetbackend.info.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "InfoShelterEntity")
@Table(name = "SHELTER")
@Data
@NoArgsConstructor
public class ShelterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelter_id")
    private Long shelterId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "shelter_name")
    private String shelterName;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "capacity")
    private Integer capacity;
    
    @Column(name = "operating_hours")
    private String operatingHours;
    
    @Column(name = "rename_filename")
    private String renameFilename;
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    // 조인을 통해 USERS 테이블에서 가져올 정보들을 위한 @Transient 필드들
    @Transient
    private String managerName; // users.user_name
    
    @Transient
    private String role; // users.role (shelter인지 확인용)
    
    @Transient
    private String status; // users.status (active인지 확인용)
    
    // 프론트엔드 호환성을 위한 필드들
    @Transient
    private String name; // shelterName의 별칭
    
    @Transient
    private Integer currentAnimals; // 현재 보호중인 동물 수 (계산 필요)
    
    @Transient
    private Double rating; // 평점 (기본값 또는 계산)
    
    @Transient
    private String type; // 보호소 유형 추론 (공공/민간/단체)
    
    // 프론트엔드 호환성을 위한 getter
    public String getName() {
        return this.shelterName;
    }
    
    public void setName(String name) {
        this.shelterName = name;
        this.name = name;
    }
    
    // 기본 rating 설정
    public Double getRating() {
        return this.rating != null ? this.rating : 4.0;
    }
    
    // 보호소 유형 추론 로직
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        
        if (this.shelterName != null) {
            String lowerName = this.shelterName.toLowerCase();
            if (lowerName.contains("시") && (lowerName.contains("보호센터") || lowerName.contains("동물보호"))) {
                return "public"; // 공공보호소
            } else if (lowerName.contains("협회") || lowerName.contains("단체") || lowerName.contains("사단법인")) {
                return "organization"; // 단체보호소
            } else {
                return "private"; // 민간보호소
            }
        }
        
        return "private"; // 기본값
    }
}