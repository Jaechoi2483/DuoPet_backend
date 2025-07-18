package com.petlogue.duopetbackend.info.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petlogue.duopetbackend.info.jpa.entity.ShelterInfo;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 보호소 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShelterInfoDto {
    
    private Long shelterInfoId;
    private String careRegNo;
    private String careNm;
    private String orgNm;
    private String divisionNm;
    private String saveTrgtAnimal;
    private String careAddr;
    private String jibunAddr;
    private Double lat;
    private Double lng;
    private String careTel;
    private LocalDate dsignationDate;
    private String weekOprStime;
    private String weekOprEtime;
    private String closeDay;
    private Integer vetPersonCnt;
    private Integer specsPersonCnt;
    private LocalDate dataStdDt;
    
    // 추가 정보
    private String shelterType; // 공공/민간/기타
    private boolean acceptsDogs;
    private boolean acceptsCats;
    private Double distance; // 거리 (위치 기반 검색시)
    
    // Entity -> DTO 변환
    public static ShelterInfoDto fromEntity(ShelterInfo entity) {
        return ShelterInfoDto.builder()
                .shelterInfoId(entity.getShelterInfoId())
                .careRegNo(entity.getCareRegNo())
                .careNm(entity.getCareNm())
                .orgNm(entity.getOrgNm())
                .divisionNm(entity.getDivisionNm())
                .saveTrgtAnimal(entity.getSaveTrgtAnimal())
                .careAddr(entity.getCareAddr())
                .jibunAddr(entity.getJibunAddr())
                .lat(entity.getLat())
                .lng(entity.getLng())
                .careTel(entity.getCareTel())
                .dsignationDate(entity.getDsignationDate())
                .weekOprStime(entity.getWeekOprStime())
                .weekOprEtime(entity.getWeekOprEtime())
                .closeDay(entity.getCloseDay())
                .vetPersonCnt(entity.getVetPersonCnt())
                .specsPersonCnt(entity.getSpecsPersonCnt())
                .dataStdDt(entity.getDataStdDt())
                .shelterType(entity.getShelterType())
                .acceptsDogs(entity.acceptsDogs())
                .acceptsCats(entity.acceptsCats())
                .build();
    }
    
    // DTO -> Entity 변환
    public ShelterInfo toEntity() {
        return ShelterInfo.builder()
                .careRegNo(this.careRegNo)
                .careNm(this.careNm)
                .orgNm(this.orgNm)
                .divisionNm(this.divisionNm)
                .saveTrgtAnimal(this.saveTrgtAnimal)
                .careAddr(this.careAddr)
                .jibunAddr(this.jibunAddr)
                .lat(this.lat)
                .lng(this.lng)
                .careTel(this.careTel)
                .dsignationDate(this.dsignationDate)
                .weekOprStime(this.weekOprStime)
                .weekOprEtime(this.weekOprEtime)
                .closeDay(this.closeDay)
                .vetPersonCnt(this.vetPersonCnt)
                .specsPersonCnt(this.specsPersonCnt)
                .dataStdDt(this.dataStdDt)
                .build();
    }
    
    /**
     * 공공데이터 API 응답 파싱용 DTO
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponseDto {
        private ResponseDto response;
        
        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ResponseDto {
            private HeaderDto header;
            private BodyDto body;
        }
        
        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class HeaderDto {
            private String resultCode;
            private String resultMsg;
        }
        
        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BodyDto {
            private ItemsDto items;
            private Integer numOfRows;
            private Integer pageNo;
            private Integer totalCount;
        }
        
        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ItemsDto {
            private List<ItemDto> item;
        }
        
        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ItemDto {
            private String careNm;
            private String careRegNo;
            private String orgNm;
            private String divisionNm;
            private String saveTrgtAnimal;
            private String careAddr;
            private String jibunAddr;
            
            @JsonProperty("lat")
            private String latStr;
            
            @JsonProperty("lng")
            private String lngStr;
            
            private String careTel;
            private String dsignationDate;
            private String weekOprStime;
            private String weekOprEtime;
            private String closeDay;
            
            @JsonProperty("vetPersonCnt")
            private String vetPersonCntStr;
            
            @JsonProperty("specsPersonCnt")
            private String specsPersonCntStr;
            
            private String dataStdDt;
            
            // String -> 적절한 타입으로 변환
            public Double getLatAsDouble() {
                try {
                    return latStr != null ? Double.parseDouble(latStr) : null;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            
            public Double getLngAsDouble() {
                try {
                    return lngStr != null ? Double.parseDouble(lngStr) : null;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            
            public Integer getVetPersonCntAsInteger() {
                try {
                    return vetPersonCntStr != null ? Integer.parseInt(vetPersonCntStr) : 0;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            
            public Integer getSpecsPersonCntAsInteger() {
                try {
                    return specsPersonCntStr != null ? Integer.parseInt(specsPersonCntStr) : 0;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            
            public LocalDate getParsedDsignationDate() {
                try {
                    return dsignationDate != null ? 
                        LocalDate.parse(dsignationDate, DateTimeFormatter.ISO_DATE) : null;
                } catch (Exception e) {
                    return null;
                }
            }
            
            public LocalDate getParsedDataStdDt() {
                try {
                    return dataStdDt != null ? 
                        LocalDate.parse(dataStdDt, DateTimeFormatter.ISO_DATE) : null;
                } catch (Exception e) {
                    return null;
                }
            }
            
            // API 응답 -> Entity 변환
            public ShelterInfo toEntity() {
                return ShelterInfo.builder()
                        .careRegNo(this.careRegNo)
                        .careNm(this.careNm)
                        .orgNm(this.orgNm)
                        .divisionNm(this.divisionNm)
                        .saveTrgtAnimal(this.saveTrgtAnimal)
                        .careAddr(this.careAddr)
                        .jibunAddr(this.jibunAddr)
                        .lat(this.getLatAsDouble())
                        .lng(this.getLngAsDouble())
                        .careTel(this.careTel)
                        .dsignationDate(this.getParsedDsignationDate())
                        .weekOprStime(this.weekOprStime)
                        .weekOprEtime(this.weekOprEtime)
                        .closeDay(this.closeDay)
                        .vetPersonCnt(this.getVetPersonCntAsInteger())
                        .specsPersonCnt(this.getSpecsPersonCntAsInteger())
                        .dataStdDt(this.getParsedDataStdDt())
                        .build();
            }
        }
    }
    
    /**
     * CSV 파일 파싱용 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsvImportDto {
        private String careNm;
        private String careRegNo;
        private String orgNm;
        private String divisionNm;
        private String saveTrgtAnimal;
        private String careAddr;
        private String jibunAddr;
        private String lat;
        private String lng;
        private String dsignationDate;
        private String weekOprStime;
        private String weekOprEtime;
        private String closeDay;
        private String vetPersonCnt;
        private String specsPersonCnt;
        private String careTel;
        private String dataStdDt;
        
        // CSV 라인 파싱
        public static CsvImportDto fromCsvLine(String[] line) {
            if (line.length < 17) return null;
            
            return new CsvImportDto(
                    line[0],  // careNm
                    line[1],  // careRegNo
                    line[2],  // orgNm
                    line[3],  // divisionNm
                    line[4],  // saveTrgtAnimal
                    line[5],  // careAddr
                    line[6],  // jibunAddr
                    line[7],  // lat
                    line[8],  // lng
                    line[9],  // dsignationDate
                    line[10], // weekOprStime
                    line[11], // weekOprEtime
                    line[12], // closeDay
                    line[13], // vetPersonCnt
                    line[14], // specsPersonCnt
                    line[15], // careTel
                    line[16]  // dataStdDt
            );
        }
        
        // 유효성 검증
        public boolean isValid() {
            return careRegNo != null && !careRegNo.trim().isEmpty() &&
                   careNm != null && !careNm.trim().isEmpty();
        }
    }
}