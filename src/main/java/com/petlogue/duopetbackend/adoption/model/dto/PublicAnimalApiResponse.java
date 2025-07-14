package com.petlogue.duopetbackend.adoption.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
        
        // 나이 파싱 (개선된 로직)
        dto.setAge(parseAge(item.getAge()));
        
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
        dto.setOrgNm(item.getOrgNm());
        
        return dto;
    }
    
    /**
     * 개선된 나이 파싱 로직
     * 공공 API에서 받아온 나이 데이터를 안전하게 파싱합니다.
     * @param ageData 공공 API의 age 필드 값 (예: "2024(년생)", "3", "6개월")
     * @return 파싱된 나이 (실패시 null)
     */
    private static Integer parseAge(String ageData) {
        if (ageData == null || ageData.trim().isEmpty()) {
            return null;
        }
        
        try {
            String cleanData = ageData.trim();
            
            // 1. "2024(년생)" 형태의 출생년도 패턴 처리 (최우선)
            Pattern birthYearPattern = Pattern.compile("(\\d{4})\\s*\\(\\s*년생\\s*\\)");
            Matcher birthYearMatcher = birthYearPattern.matcher(cleanData);
            
            if (birthYearMatcher.find()) {
                int birthYear = Integer.parseInt(birthYearMatcher.group(1));
                int currentYear = java.time.Year.now().getValue();
                int age = currentYear - birthYear;
                
                // 합리적인 나이 범위 체크 (출생년도가 1990-현재년도 범위)
                if (birthYear >= 1990 && birthYear <= currentYear && age >= 0 && age <= 35) {
                    return age;
                }
                // 범위를 벗어나면 로깅하고 null 반환
                return null;
            }
            
            // 2. 일반적인 4자리 연도 패턴 (년생 표기 없이)
            Pattern yearPattern = Pattern.compile("\\b(20[0-2][0-9])\\b");
            Matcher yearMatcher = yearPattern.matcher(cleanData);
            
            if (yearMatcher.find()) {
                int birthYear = Integer.parseInt(yearMatcher.group(1));
                int currentYear = java.time.Year.now().getValue();
                int age = currentYear - birthYear;
                
                // 합리적인 나이 범위 체크
                if (age >= 0 && age <= 35) {
                    return age;
                }
            }
            
            // 3. "개월" 단위 처리 (예: "6개월" -> 0세)
            Pattern monthPattern = Pattern.compile("(\\d+)\\s*개월");
            Matcher monthMatcher = monthPattern.matcher(cleanData);
            
            if (monthMatcher.find()) {
                int months = Integer.parseInt(monthMatcher.group(1));
                if (months >= 0 && months <= 60) { // 최대 5년까지
                    return months / 12; // 12개월 미만은 0세로 처리
                }
            }
            
            // 4. 단순 나이 숫자 패턴 (독립된 숫자, 0-30 범위)
            Pattern simpleAgePattern = Pattern.compile("\\b([0-9]|[1-2][0-9]|30)\\b");
            Matcher simpleAgeMatcher = simpleAgePattern.matcher(cleanData);
            
            if (simpleAgeMatcher.find()) {
                int age = Integer.parseInt(simpleAgeMatcher.group(1));
                return age;
            }
            
            return null;
            
        } catch (NumberFormatException e) {
            // 숫자 파싱 실패
            return null;
        } catch (Exception e) {
            // 기타 파싱 실패
            return null;
        }
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