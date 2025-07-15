package com.petlogue.duopetbackend.info.model.dto;

import com.petlogue.duopetbackend.info.jpa.entity.AnimalHospital;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalHospitalDto {
    
    private Long hospitalId;
    private String managementNo;
    private String businessName;
    private String roadAddress;
    private String jibunAddress;
    private String phone;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private String businessStatus;
    private String detailedStatus;
    private LocalDate licenseDate;
    private String city;
    private String district;
    private String areaSize;
    private Integer employeeCount;
    
    // 추가 표시용 필드
    private Double distance; // 사용자 위치로부터의 거리 (km)
    private boolean hasPhone;
    private boolean hasLocation;
    
    // Entity -> DTO 변환
    public static AnimalHospitalDto from(AnimalHospital entity) {
        return AnimalHospitalDto.builder()
                .hospitalId(entity.getHospitalId())
                .managementNo(entity.getManagementNo())
                .businessName(entity.getBusinessName())
                .roadAddress(entity.getRoadAddress())
                .jibunAddress(entity.getJibunAddress())
                .phone(entity.getPhone())
                .postalCode(entity.getDisplayPostalCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .businessStatus(entity.getBusinessStatus())
                .detailedStatus(entity.getDetailedStatus())
                .licenseDate(entity.getLicenseDate())
                .city(entity.getCity())
                .district(entity.getDistrict())
                .areaSize(entity.getAreaSize())
                .employeeCount(entity.getEmployeeCount())
                .hasPhone(entity.hasContactInfo())
                .hasLocation(entity.hasValidLocation())
                .build();
    }
    
    // CSV Import용 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CsvImportDto {
        private String 번호;
        private String 개방서비스명;
        private String 개방서비스아이디;
        private String 개방자치단체코드;
        private String 관리번호;
        private String 인허가일자;
        private String 인허가취소일자;
        private String 영업상태구분코드;
        private String 영업상태명;
        private String 상세영업상태코드;
        private String 상세영업상태명;
        private String 폐업일자;
        private String 휴업시작일자;
        private String 휴업종료일자;
        private String 재개업일자;
        private String 소재지전화;
        private String 소재지면적;
        private String 소재지우편번호;
        private String 소재지전체주소;
        private String 도로명전체주소;
        private String 도로명우편번호;
        private String 사업장명;
        private String 최종수정시점;
        private String 데이터갱신구분;
        private String 데이터갱신일자;
        private String 업태구분명;
        private String 좌표정보x;
        private String 좌표정보y;
        private String 업무구분명;
        private String 상세업무구분명;
        private String 권리주체일련번호;
        private String 총직원수;
        
        // 데이터 품질 검증
        public boolean isValid() {
            // 영업 중이고 정상 상태인지 확인
            if (!"영업/정상".equals(영업상태명) || !"정상".equals(상세영업상태명)) {
                return false;
            }
            
            // 병원명이 있는지 확인
            if (사업장명 == null || 사업장명.trim().isEmpty()) {
                return false;
            }
            
            // 주소가 하나라도 있는지 확인
            boolean hasAddress = (도로명전체주소 != null && !도로명전체주소.trim().isEmpty()) ||
                               (소재지전체주소 != null && !소재지전체주소.trim().isEmpty());
            
            if (!hasAddress) {
                return false;
            }
            
            // 좌표 정보가 있는지 확인 (선택적)
            // 좌표가 없어도 주소가 있으면 OK
            
            return true;
        }
        
        // 시도명 추출
        public String extractCity() {
            String address = 도로명전체주소 != null ? 도로명전체주소 : 소재지전체주소;
            if (address == null) return null;
            
            String[] parts = address.split(" ");
            if (parts.length > 0) {
                return parts[0];
            }
            return null;
        }
        
        // 시군구명 추출
        public String extractDistrict() {
            String address = 도로명전체주소 != null ? 도로명전체주소 : 소재지전체주소;
            if (address == null) return null;
            
            String[] parts = address.split(" ");
            if (parts.length > 1) {
                return parts[1];
            }
            return null;
        }
    }
}