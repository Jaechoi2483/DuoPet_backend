package com.petlogue.duopetbackend.adoption.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class PublicAnimalApiResponse {
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String desertionNo;      // 유기번호
        private String filename;         // 썸네일 이미지
        private String happenDt;         // 접수일 (YYYYMMDD)
        private String happenPlace;      // 발견장소
        private String kindCd;           // 품종
        private String colorCd;          // 색상
        private String age;              // 나이
        private String weight;           // 체중
        private String noticeNo;         // 공고번호
        private String noticeSdt;        // 공고시작일
        private String noticeEdt;        // 공고종료일
        private String popfile;          // 이미지
        private String popfile1;         // 고품질 이미지 1
        private String popfile2;         // 고품질 이미지 2
        private String processState;     // 상태
        private String sexCd;            // 성별 (M/F/Q)
        private String neuterYn;         // 중성화여부 (Y/N/U)
        private String specialMark;      // 특징
        private String careNm;           // 보호소이름
        private String careTel;          // 보호소전화번호
        private String careAddr;         // 보호소주소
        private String orgNm;            // 관할기관
        private String chargeNm;         // 담당자
        private String officetel;        // 담당자연락처
        private String noticeComment;    // 특이사항
        private String kindNm;           // 품종명 (e.g., "말티즈", "코리안숏헤어")
        private String upKindNm;         // 축종명 (e.g., "개", "고양이")
        private String careRegNo;        // 보호소등록번호
    }
    
    // API 응답을 AdoptionAnimal 엔티티로 변환
    public static AdoptionAnimalDto toDto(Item item) {
        AdoptionAnimalDto dto = new AdoptionAnimalDto();
        
        // 기본 정보
        dto.setDesertionNo(item.getDesertionNo());
        
        // 이미지 URL 우선순위: popfile1 > popfile > filename
        String imageUrl = null;
        if (item.getPopfile1() != null && !item.getPopfile1().trim().isEmpty()) {
            imageUrl = item.getPopfile1();
        } else if (item.getPopfile() != null && !item.getPopfile().trim().isEmpty()) {
            imageUrl = item.getPopfile();
        } else if (item.getFilename() != null && !item.getFilename().trim().isEmpty()) {
            imageUrl = item.getFilename();
        }
        dto.setImageUrl(imageUrl);
        
        dto.setHappenPlace(item.getHappenPlace());
        dto.setSpecialMark(item.getSpecialMark());
        dto.setPublicNoticeNo(item.getNoticeNo());
        dto.setColorCd(item.getColorCd());
        dto.setProcessState(item.getProcessState());
        
        // 동물 종류 설정 - upKindNm 우선 사용
        if (item.getUpKindNm() != null && !item.getUpKindNm().trim().isEmpty()) {
            dto.setAnimalType(item.getUpKindNm());
        } else if (item.getKindCd() != null && item.getKindCd().contains("[")) {
            // kindCd에서 추출 (예: "[개] 믹스견" -> "개")
            String[] parts = item.getKindCd().split("] ");
            if (parts.length > 0) {
                dto.setAnimalType(parts[0].replace("[", "").trim());
            }
        } else {
            dto.setAnimalType("기타"); // 기본값
        }
        
        // 품종 정보 설정 - kindNm 우선 사용
        if (item.getKindNm() != null && !item.getKindNm().trim().isEmpty()) {
            dto.setBreed(item.getKindNm());
        } else if (item.getKindCd() != null && item.getKindCd().contains("] ")) {
            // kindCd에서 추출 (예: "[개] 믹스견" -> "믹스견")
            String[] parts = item.getKindCd().split("] ");
            if (parts.length > 1) {
                dto.setBreed(parts[1].trim());
            }
        }
        
        // 성별 변환
        dto.setGender(item.getSexCd());
        
        // 중성화 여부 - U(불명)는 N으로 처리
        String neuterYn = item.getNeuterYn();
        if ("Y".equals(neuterYn)) {
            dto.setNeutered("Y");
        } else {
            dto.setNeutered("N");  // U 또는 기타 값은 모두 N으로 처리
        }
        
        // 나이 파싱 (문자열에서 숫자 추출 시도)
        if (item.getAge() != null) {
            try {
                // "2023(년생)" 같은 형태에서 숫자만 추출
                String ageStr = item.getAge().replaceAll("[^0-9]", "");
                if (!ageStr.isEmpty()) {
                    int birthYear = Integer.parseInt(ageStr);
                    if (birthYear > 2000) { // 년도인 경우
                        dto.setAge(java.time.Year.now().getValue() - birthYear);
                    } else { // 나이인 경우
                        dto.setAge(birthYear);
                    }
                }
            } catch (Exception e) {
                // 파싱 실패시 null로 유지
            }
        }
        
        // 체중 파싱
        if (item.getWeight() != null) {
            try {
                String weightStr = item.getWeight().replaceAll("[^0-9.]", "");
                if (!weightStr.isEmpty()) {
                    dto.setWeight(Double.parseDouble(weightStr));
                }
            } catch (Exception e) {
                // 파싱 실패시 null로 유지
            }
        }
        
        // 날짜 변환 (YYYYMMDD -> LocalDate)
        dto.setHappenDate(parseDate(item.getHappenDt()));
        dto.setPublicNoticeStart(parseDate(item.getNoticeSdt()));
        dto.setPublicNoticeEnd(parseDate(item.getNoticeEdt()));
        
        // 보호소 정보
        dto.setShelterName(item.getCareNm());
        dto.setShelterPhone(item.getCareTel());
        dto.setShelterAddress(item.getCareAddr());
        
        return dto;
    }
    
    private static java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(dateStr, 
                java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}