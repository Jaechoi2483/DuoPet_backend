package com.petlogue.duopetbackend.info.model.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.petlogue.duopetbackend.info.jpa.entity.AnimalHospital;
import com.petlogue.duopetbackend.info.jpa.repository.AnimalHospitalRepository;
import com.petlogue.duopetbackend.info.model.dto.AnimalHospitalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnimalHospitalService {
    
    private final AnimalHospitalRepository hospitalRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * CSV 파일 임포트
     */
    @Transactional
    public Map<String, Object> importFromCsv(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int totalCount = 0;
        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;
        
        try (CSVReader csvReader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")))
                .withSkipLines(1) // 헤더 스킵
                .build()) {
            
            String[] nextLine;
            int lineNumber = 1;
            
            while ((nextLine = csvReader.readNext()) != null) {
                lineNumber++;
                totalCount++;
                
                try {
                    // CSV 라인을 DTO로 변환
                    AnimalHospitalDto.CsvImportDto csvDto = parseCsvLine(nextLine);
                    
                    // 데이터 품질 검증
                    if (!csvDto.isValid()) {
                        skipCount++;
                        log.debug("Line {} skipped: invalid data", lineNumber);
                        continue;
                    }
                    
                    // 중복 체크 (관리번호)
                    if (hospitalRepository.findByManagementNo(csvDto.get관리번호()).isPresent()) {
                        skipCount++;
                        log.debug("Line {} skipped: duplicate management number", lineNumber);
                        continue;
                    }
                    
                    // Entity 생성 및 저장
                    AnimalHospital hospital = createHospitalFromCsv(csvDto);
                    hospitalRepository.save(hospital);
                    successCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = String.format("Line %d error: %s", lineNumber, e.getMessage());
                    errors.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }
            
        } catch (Exception e) {
            log.error("CSV import failed", e);
            result.put("success", false);
            result.put("message", "CSV 파일 처리 중 오류 발생: " + e.getMessage());
            return result;
        }
        
        result.put("success", true);
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);
        result.put("message", String.format("총 %d건 중 %d건 성공, %d건 스킵, %d건 실패", 
                                          totalCount, successCount, skipCount, errorCount));
        
        return result;
    }
    
    /**
     * CSV 라인 파싱
     */
    private AnimalHospitalDto.CsvImportDto parseCsvLine(String[] line) {
        if (line.length < 29) {
            throw new IllegalArgumentException("CSV 컬럼 수가 부족합니다");
        }
        
        return AnimalHospitalDto.CsvImportDto.builder()
                .번호(line[0])
                .개방서비스명(line[1])
                .개방서비스아이디(line[2])
                .개방자치단체코드(line[3])
                .관리번호(line[4])
                .인허가일자(line[5])
                .인허가취소일자(line[6])
                .영업상태구분코드(line[7])
                .영업상태명(line[8])
                .상세영업상태코드(line[9])
                .상세영업상태명(line[10])
                .폐업일자(line[11])
                .휴업시작일자(line[12])
                .휴업종료일자(line[13])
                .재개업일자(line[14])
                .소재지전화(line[15])
                .소재지면적(line[16])
                .소재지우편번호(line[17])
                .소재지전체주소(line[18])
                .도로명전체주소(line[19])
                .도로명우편번호(line[20])
                .사업장명(line[21])
                .최종수정시점(line[22])
                .데이터갱신구분(line[23])
                .데이터갱신일자(line[24])
                .업태구분명(line[25])
                .좌표정보x(line[26])
                .좌표정보y(line[27])
                .총직원수(line.length > 28 ? line[28] : null)
                .build();
    }
    
    /**
     * CSV DTO를 Entity로 변환
     */
    private AnimalHospital createHospitalFromCsv(AnimalHospitalDto.CsvImportDto csv) {
        AnimalHospital.AnimalHospitalBuilder builder = AnimalHospital.builder()
                .managementNo(csv.get관리번호())
                .businessName(csv.get사업장명())
                .roadAddress(csv.get도로명전체주소())
                .jibunAddress(csv.get소재지전체주소())
                .phone(normalizePhone(csv.get소재지전화()))
                .roadPostalCode(csv.get도로명우편번호())
                .jibunPostalCode(csv.get소재지우편번호())
                .businessStatus(csv.get영업상태명())
                .businessStatusCode(csv.get영업상태구분코드())
                .detailedStatus(csv.get상세영업상태명())
                .detailedStatusCode(csv.get상세영업상태코드())
                .city(csv.extractCity())
                .district(csv.extractDistrict())
                .areaSize(csv.get소재지면적())
                .dataUpdateType(csv.get데이터갱신구분());
        
        // 좌표 변환 (EPSG:5174 -> WGS84)
        if (csv.get좌표정보x() != null && csv.get좌표정보y() != null) {
            try {
                double[] wgs84 = convertEPSG5174ToWGS84(
                    Double.parseDouble(csv.get좌표정보x()),
                    Double.parseDouble(csv.get좌표정보y())
                );
                builder.latitude(wgs84[0]);
                builder.longitude(wgs84[1]);
                builder.epsg5174X(csv.get좌표정보x());
                builder.epsg5174Y(csv.get좌표정보y());
            } catch (NumberFormatException e) {
                log.debug("좌표 변환 실패: {}, {}", csv.get좌표정보x(), csv.get좌표정보y());
            }
        }
        
        // 날짜 파싱
        builder.licenseDate(parseDate(csv.get인허가일자()));
        builder.closedDate(parseDate(csv.get폐업일자()));
        builder.suspendedStartDate(parseDate(csv.get휴업시작일자()));
        builder.suspendedEndDate(parseDate(csv.get휴업종료일자()));
        builder.reopenedDate(parseDate(csv.get재개업일자()));
        builder.dataUpdateDate(parseDateTime(csv.get데이터갱신일자()));
        
        // 직원 수 파싱
        if (csv.get총직원수() != null && !csv.get총직원수().trim().isEmpty()) {
            try {
                builder.employeeCount(Integer.parseInt(csv.get총직원수()));
            } catch (NumberFormatException e) {
                log.debug("직원수 파싱 실패: {}", csv.get총직원수());
            }
        }
        
        return builder.build();
    }
    
    /**
     * EPSG:5174 좌표계를 WGS84로 변환
     * 중부원점(Korean Central Belt 2000)
     * 간소화된 변환 공식 사용 (한국 지역에 최적화)
     */
    private double[] convertEPSG5174ToWGS84(double x, double y) {
        // TM Projection parameters for Central Belt
        double falseEasting = 200000.0;
        double falseNorthing = 500000.0;
        double centralMeridian = 127.0;
        double latitudeOfOrigin = 38.0;
        
        // Remove false easting and northing
        double E = x - falseEasting;
        double N = y - falseNorthing;
        
        // 근사 변환 공식 (한국 지역에 충분히 정확함)
        // 1도당 약 111km 기준
        double lat = latitudeOfOrigin + (N / 111000.0);
        double lon = centralMeridian + (E / (111000.0 * Math.cos(Math.toRadians(lat))));
        
        // 미세 조정 (경험적 보정값)
        lat = lat - 0.0023;  // 약 250m 남쪽 보정
        lon = lon - 0.0008;  // 약 90m 서쪽 보정
        
        return new double[]{lat, lon};
    }
    
    /**
     * 전화번호 정규화
     */
    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        // 숫자와 하이픈만 남기기
        String normalized = phone.replaceAll("[^0-9-]", "");
        
        // 빈 문자열이면 null 반환
        if (normalized.isEmpty()) {
            return null;
        }
        
        return normalized;
    }
    
    /**
     * 날짜 파싱
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.debug("날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }
    
    /**
     * 날짜시간 파싱
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            LocalDate date = LocalDate.parse(dateTimeStr.substring(0, 10), DATE_FORMATTER);
            return date.atStartOfDay();
        } catch (Exception e) {
            log.debug("날짜시간 파싱 실패: {}", dateTimeStr);
            return null;
        }
    }
    
    /**
     * 영업 중인 병원 조회
     */
    @Transactional(readOnly = true)
    public Page<AnimalHospitalDto> getOperatingHospitals(Pageable pageable) {
        return hospitalRepository.findOperatingHospitals(pageable)
                .map(AnimalHospitalDto::from);
    }
    
    /**
     * 키워드 검색
     */
    @Transactional(readOnly = true)
    public Page<AnimalHospitalDto> searchByKeyword(String keyword, Pageable pageable) {
        return hospitalRepository.searchByKeyword(keyword, pageable)
                .map(AnimalHospitalDto::from);
    }
    
    /**
     * 시도별 병원 조회
     */
    @Transactional(readOnly = true)
    public Page<AnimalHospitalDto> getHospitalsByCity(String city, Pageable pageable) {
        return hospitalRepository.findByCity(city, pageable)
                .map(AnimalHospitalDto::from);
    }
    
    /**
     * 시군구별 병원 조회
     */
    @Transactional(readOnly = true)
    public Page<AnimalHospitalDto> getHospitalsByCityAndDistrict(String city, String district, Pageable pageable) {
        return hospitalRepository.findByCityAndDistrict(city, district, pageable)
                .map(AnimalHospitalDto::from);
    }
    
    /**
     * 근처 병원 찾기
     */
    @Transactional(readOnly = true)
    public List<AnimalHospitalDto> getNearbyHospitals(double latitude, double longitude, double radiusInKm) {
        return hospitalRepository.findNearbyHospitals(latitude, longitude, radiusInKm)
                .stream()
                .map(AnimalHospitalDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalOperating", hospitalRepository.count());
        stats.put("withPhone", hospitalRepository.countOperatingHospitalsWithPhone());
        stats.put("withCoordinates", hospitalRepository.countOperatingHospitalsWithCoordinates());
        
        List<Map<String, Object>> cityStats = new ArrayList<>();
        for (Object[] row : hospitalRepository.countByCityGrouped()) {
            Map<String, Object> cityStat = new HashMap<>();
            cityStat.put("city", row[0]);
            cityStat.put("count", row[1]);
            cityStats.add(cityStat);
        }
        stats.put("byCity", cityStats);
        
        return stats;
    }
    
    /**
     * 모든 병원의 좌표를 재계산하여 업데이트
     */
    @Transactional
    public Map<String, Object> recalculateAllCoordinates() {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        int totalCount = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // 모든 영업 중인 병원 조회
            Page<AnimalHospital> allHospitals = hospitalRepository.findOperatingHospitals(
                PageRequest.of(0, 10000) // 모든 데이터 조회
            );
            
            totalCount = (int) allHospitals.getTotalElements();
            
            for (AnimalHospital hospital : allHospitals.getContent()) {
                try {
                    // EPSG:5174 좌표가 있는 경우에만 재계산
                    if (hospital.getEpsg5174X() != null && hospital.getEpsg5174Y() != null) {
                        double x = Double.parseDouble(hospital.getEpsg5174X());
                        double y = Double.parseDouble(hospital.getEpsg5174Y());
                        
                        // 좌표 재계산
                        double[] wgs84 = convertEPSG5174ToWGS84(x, y);
                        
                        // 업데이트
                        hospital.setLatitude(wgs84[0]);
                        hospital.setLongitude(wgs84[1]);
                        hospitalRepository.save(hospital);
                        
                        successCount++;
                        
                        // 진행상황 로그
                        if (successCount % 100 == 0) {
                            log.info("좌표 재계산 진행중: {}/{}", successCount, totalCount);
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = String.format("병원 ID %d 좌표 변환 실패: %s", 
                        hospital.getHospitalId(), e.getMessage());
                    errors.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }
            
            result.put("success", true);
            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errors", errors);
            result.put("message", String.format("총 %d개 중 %d개 성공, %d개 실패", 
                                              totalCount, successCount, errorCount));
            
        } catch (Exception e) {
            log.error("좌표 재계산 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "좌표 재계산 중 오류 발생: " + e.getMessage());
        }
        
        return result;
    }
}