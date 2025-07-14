package com.petlogue.duopetbackend.info.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnimalHospitalSchedulerService {
    
    private final AnimalHospitalService hospitalService;
    private static final String CSV_DOWNLOAD_URL = "https://www.data.go.kr/download/15045050/standard.do?dataType=csv";
    
    /**
     * 매월 1일 새벽 3시에 실행
     * 공공데이터포털에서 최신 CSV 다운로드 후 자동 임포트
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void updateHospitalData() {
        log.info("동물병원 데이터 자동 갱신 시작: {}", LocalDateTime.now());
        
        try {
            // 1. CSV 파일 다운로드
            Path tempFile = downloadCsvFile();
            
            // 2. MultipartFile로 변환하여 임포트
            // 실제 구현 시 MockMultipartFile 또는 custom implementation 필요
            log.info("CSV 파일 다운로드 완료: {}", tempFile);
            
            // 3. 임포트 결과 로깅
            // Map<String, Object> result = hospitalService.importFromCsv(multipartFile);
            // log.info("동물병원 데이터 갱신 완료: {}", result);
            
            // 4. 임시 파일 삭제
            Files.deleteIfExists(tempFile);
            
        } catch (Exception e) {
            log.error("동물병원 데이터 자동 갱신 실패", e);
        }
    }
    
    /**
     * 공공데이터포털에서 CSV 파일 다운로드
     */
    private Path downloadCsvFile() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        
        // 임시 파일 생성
        Path tempFile = Files.createTempFile("animal_hospitals_", ".csv");
        
        // 다운로드 (실제 URL과 인증 방식은 공공데이터포털 API에 맞게 조정 필요)
        URI uri = new URI(CSV_DOWNLOAD_URL);
        byte[] csvData = restTemplate.getForObject(uri, byte[].class);
        
        // 파일 저장
        Files.write(tempFile, csvData);
        
        return tempFile;
    }
    
    /**
     * 수동 갱신 트리거
     */
    public void triggerManualUpdate() {
        log.info("동물병원 데이터 수동 갱신 시작");
        updateHospitalData();
    }
}