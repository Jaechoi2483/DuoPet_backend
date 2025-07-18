package com.petlogue.duopetbackend.info.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공공데이터 API로부터 수집한 보호소 정보를 저장하는 엔티티
 * 기존 Shelter 엔티티와는 별개로 순수 보호소 정보만 관리
 */
@Entity
@Table(name = "SHELTER_INFO", indexes = {
    @Index(name = "idx_shelter_info_location", columnList = "lat, lng"),
    @Index(name = "idx_shelter_info_org", columnList = "org_nm"),
    @Index(name = "idx_shelter_info_status", columnList = "division_nm")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"createdAt", "updatedAt"})
public class ShelterInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelter_info_id")
    private Long shelterInfoId;
    
    @Column(name = "care_reg_no", unique = true, nullable = false, length = 50)
    private String careRegNo; // 관리번호 (공공데이터 PK)
    
    @Column(name = "care_nm", nullable = false, length = 200)
    private String careNm; // 보호소명
    
    @Column(name = "org_nm", length = 200)
    private String orgNm; // 관할기관
    
    @Column(name = "division_nm", length = 50)
    private String divisionNm; // 보호소구분 (법인/개인/기타)
    
    @Column(name = "save_trgt_animal", length = 100)
    private String saveTrgtAnimal; // 보호동물 (개+고양이+기타)
    
    @Column(name = "care_addr", length = 500)
    private String careAddr; // 도로명주소
    
    @Column(name = "jibun_addr", length = 500)
    private String jibunAddr; // 지번주소
    
    @Column(name = "lat")
    private Double lat; // 위도 (WGS84)
    
    @Column(name = "lng")
    private Double lng; // 경도 (WGS84)
    
    @Column(name = "care_tel", length = 50)
    private String careTel; // 전화번호
    
    @Column(name = "dsignation_date")
    private LocalDate dsignationDate; // 지정일자
    
    @Column(name = "week_opr_stime", length = 10)
    private String weekOprStime; // 평일운영시작시간
    
    @Column(name = "week_opr_etime", length = 10)
    private String weekOprEtime; // 평일운영종료시간
    
    @Column(name = "close_day", length = 100)
    private String closeDay; // 휴무일
    
    @Column(name = "vet_person_cnt")
    private Integer vetPersonCnt; // 수의사수
    
    @Column(name = "specs_person_cnt")
    private Integer specsPersonCnt; // 사양관리사수
    
    @Column(name = "data_std_dt")
    private LocalDate dataStdDt; // 데이터기준일자
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직
    public boolean isOperatingNow() {
        // 현재 운영중인지 확인하는 로직
        // TODO: 구현 필요
        return true;
    }
    
    public String getShelterType() {
        // 보호소 타입 반환 (공공/민간/기타)
        if (divisionNm != null) {
            if (divisionNm.contains("법인")) return "법인";
            if (divisionNm.contains("개인")) return "개인";
            if (divisionNm.contains("단체")) return "단체";
        }
        return "기타";
    }
    
    public boolean acceptsDogs() {
        return saveTrgtAnimal != null && saveTrgtAnimal.contains("개");
    }
    
    public boolean acceptsCats() {
        return saveTrgtAnimal != null && saveTrgtAnimal.contains("고양이");
    }
}