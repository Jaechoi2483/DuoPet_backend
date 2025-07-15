package com.petlogue.duopetbackend.info.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ANIMAL_HOSPITALS", indexes = {
    @Index(name = "idx_business_status", columnList = "business_status"),
    @Index(name = "idx_city_district", columnList = "city, district"),
    @Index(name = "idx_coordinates", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalHospital {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long hospitalId;
    
    @Column(name = "management_no", unique = true)
    private String managementNo; // 관리번호
    
    @Column(name = "business_name", nullable = false)
    private String businessName; // 사업장명
    
    @Column(name = "road_address")
    private String roadAddress; // 도로명주소
    
    @Column(name = "jibun_address")
    private String jibunAddress; // 지번주소
    
    @Column(name = "phone", length = 20)
    private String phone; // 전화번호
    
    @Column(name = "road_postal_code", length = 10)
    private String roadPostalCode; // 도로명우편번호
    
    @Column(name = "jibun_postal_code", length = 10)
    private String jibunPostalCode; // 지번우편번호
    
    @Column(name = "latitude")
    private Double latitude; // 위도 (WGS84)
    
    @Column(name = "longitude")
    private Double longitude; // 경도 (WGS84)
    
    @Column(name = "epsg5174_x")
    private String epsg5174X; // 원본 좌표X
    
    @Column(name = "epsg5174_y")
    private String epsg5174Y; // 원본 좌표Y
    
    @Column(name = "business_status", length = 20)
    private String businessStatus; // 영업상태명
    
    @Column(name = "business_status_code", length = 10)
    private String businessStatusCode; // 영업상태구분코드
    
    @Column(name = "detailed_status", length = 20)
    private String detailedStatus; // 상세영업상태명
    
    @Column(name = "detailed_status_code", length = 10)
    private String detailedStatusCode; // 상세영업상태코드
    
    @Column(name = "license_date")
    private LocalDate licenseDate; // 인허가일자
    
    @Column(name = "closed_date")
    private LocalDate closedDate; // 폐업일자
    
    @Column(name = "suspended_start_date")
    private LocalDate suspendedStartDate; // 휴업시작일자
    
    @Column(name = "suspended_end_date")
    private LocalDate suspendedEndDate; // 휴업종료일자
    
    @Column(name = "reopened_date")
    private LocalDate reopenedDate; // 재개업일자
    
    @Column(name = "city", length = 50)
    private String city; // 시도명
    
    @Column(name = "district", length = 50)
    private String district; // 시군구명
    
    @Column(name = "area_size")
    private String areaSize; // 소재지면적
    
    @Column(name = "employee_count")
    private Integer employeeCount; // 총직원수
    
    @Column(name = "data_source", length = 50)
    @Builder.Default
    private String dataSource = "공공데이터포털"; // 데이터 출처
    
    @Column(name = "data_update_type", length = 10)
    private String dataUpdateType; // 데이터갱신구분
    
    @Column(name = "data_update_date")
    private LocalDateTime dataUpdateDate; // 데이터갱신일자
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직 메서드
    public boolean isOperating() {
        return "영업/정상".equals(businessStatus) && "정상".equals(detailedStatus);
    }
    
    public boolean hasValidLocation() {
        return latitude != null && longitude != null;
    }
    
    public boolean hasContactInfo() {
        return phone != null && !phone.trim().isEmpty();
    }
    
    public String getDisplayAddress() {
        if (roadAddress != null && !roadAddress.trim().isEmpty()) {
            return roadAddress;
        }
        return jibunAddress;
    }
    
    public String getDisplayPostalCode() {
        if (roadPostalCode != null && !roadPostalCode.trim().isEmpty()) {
            return roadPostalCode;
        }
        return jibunPostalCode;
    }
}