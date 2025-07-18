package com.petlogue.duopetbackend.info.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlogue.duopetbackend.info.jpa.entity.ShelterInfo;
import com.petlogue.duopetbackend.info.jpa.repository.ShelterInfoRepository;
import com.petlogue.duopetbackend.info.model.dto.ShelterInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 공공데이터 API를 통한 보호소 데이터 동기화 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShelterDataSyncService {
    
    private final ShelterInfoRepository shelterInfoRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${api.animal.protection.service-key}")
    private String serviceKey;
    
    @Value("${api.animal.protection.base-url}")
    private String baseUrl;
    
    /**
     * 매월 1일 새벽 2시에 실행
     * 공공데이터포털에서 최신 보호소 정보 동기화
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void scheduledSyncShelterData() {
        log.info("보호소 데이터 자동 동기화 시작: {}", LocalDateTime.now());
        syncShelterData();
    }
    
    /**
     * 보호소 데이터 동기화 메인 로직
     */
    @Transactional
    public void syncShelterData() {
        int totalSynced = 0;
        int totalSkipped = 0;
        int totalError = 0;
        int pageNo = 1;
        boolean hasMoreData = true;
        
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // 페이징 처리로 모든 데이터 가져오기
            while (hasMoreData) {
                // 1. API 호출 준비
                String apiUrl = buildApiUrl(pageNo);
                log.info("API 호출 URL (페이지 {}): {}", pageNo, apiUrl);
                
                // 2. HttpClient로 API 호출
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Accept", "application/json")
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();
                        
                HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = httpResponse.body();
                
                log.info("Response status (페이지 {}): {}", pageNo, httpResponse.statusCode());
                
                if (httpResponse.statusCode() == 200 && responseBody != null) {
                    // 3. JSON 파싱
                    ShelterInfoDto.ApiResponseDto apiResponse = 
                        objectMapper.readValue(responseBody, ShelterInfoDto.ApiResponseDto.class);
                    
                    // 4. 응답 검증
                    if (isValidResponse(apiResponse)) {
                        List<ShelterInfoDto.ApiResponseDto.ItemDto> items = 
                            apiResponse.getResponse().getBody().getItems().getItem();
                        
                        log.info("페이지 {}: {}개의 보호소 데이터 수신", pageNo, items.size());
                        
                        // 5. 각 보호소 데이터 처리
                        for (ShelterInfoDto.ApiResponseDto.ItemDto item : items) {
                            try {
                                boolean result = processShelterData(item);
                                if (result) {
                                    totalSynced++;
                                } else {
                                    totalSkipped++;
                                }
                            } catch (Exception e) {
                                totalError++;
                                log.error("보호소 데이터 처리 중 오류 - careRegNo: {}", item.getCareRegNo(), e);
                            }
                        }
                        
                        // 다음 페이지 확인
                        int totalCount = apiResponse.getResponse().getBody().getTotalCount();
                        int numOfRows = 600;
                        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
                        
                        if (pageNo >= totalPages || items.size() < numOfRows) {
                            hasMoreData = false;
                        } else {
                            pageNo++;
                        }
                    } else {
                        log.error("API 응답이 유효하지 않습니다.");
                        hasMoreData = false;
                    }
                } else {
                    log.error("API 호출 실패 - Status: {}", httpResponse.statusCode());
                    hasMoreData = false;
                }
            }
            
        } catch (Exception e) {
            log.error("보호소 데이터 동기화 중 오류 발생", e);
        }
        
        log.info("보호소 데이터 동기화 완료 - 성공: {}, 스킵: {}, 오류: {}", 
                totalSynced, totalSkipped, totalError);
    }
    
    /**
     * API URL 빌드
     */
    private String buildApiUrl(int pageNo) {
        try {
            // 서비스 키가 이미 디코딩된 상태이므로 URL 인코딩 필요
            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
            
            // 직접 URL 구성 (PublicDataApiClient와 동일한 방식)
            String url = baseUrl + "?serviceKey=" + encodedServiceKey;
            url += "&pageNo=" + pageNo;
            url += "&numOfRows=600";
            url += "&_type=json";
            
            return url;
        } catch (Exception e) {
            log.error("URL 빌드 중 오류", e);
            throw new RuntimeException("API URL 생성 실패", e);
        }
    }
    
    /**
     * API 응답 유효성 검증
     */
    private boolean isValidResponse(ShelterInfoDto.ApiResponseDto response) {
        return response != null &&
               response.getResponse() != null &&
               response.getResponse().getHeader() != null &&
               "00".equals(response.getResponse().getHeader().getResultCode()) &&
               response.getResponse().getBody() != null &&
               response.getResponse().getBody().getItems() != null &&
               response.getResponse().getBody().getItems().getItem() != null;
    }
    
    /**
     * 개별 보호소 데이터 처리 (UPSERT)
     */
    private boolean processShelterData(ShelterInfoDto.ApiResponseDto.ItemDto item) {
        // 관리번호로 기존 데이터 확인
        Optional<ShelterInfo> existingOpt = shelterInfoRepository.findByCareRegNo(item.getCareRegNo());
        
        if (existingOpt.isPresent()) {
            // 기존 데이터 업데이트
            ShelterInfo existing = existingOpt.get();
            updateShelterInfo(existing, item);
            shelterInfoRepository.save(existing);
            log.debug("보호소 정보 업데이트 - careRegNo: {}", item.getCareRegNo());
        } else {
            // 신규 데이터 삽입
            ShelterInfo newShelter = item.toEntity();
            shelterInfoRepository.save(newShelter);
            log.debug("새로운 보호소 추가 - careRegNo: {}", item.getCareRegNo());
        }
        
        return true;
    }
    
    /**
     * 기존 엔티티 업데이트
     */
    private void updateShelterInfo(ShelterInfo existing, ShelterInfoDto.ApiResponseDto.ItemDto item) {
        existing.setCareNm(item.getCareNm());
        existing.setOrgNm(item.getOrgNm());
        existing.setDivisionNm(item.getDivisionNm());
        existing.setSaveTrgtAnimal(item.getSaveTrgtAnimal());
        existing.setCareAddr(item.getCareAddr());
        existing.setJibunAddr(item.getJibunAddr());
        existing.setLat(item.getLatAsDouble());
        existing.setLng(item.getLngAsDouble());
        existing.setCareTel(item.getCareTel());
        existing.setDsignationDate(item.getParsedDsignationDate());
        existing.setWeekOprStime(item.getWeekOprStime());
        existing.setWeekOprEtime(item.getWeekOprEtime());
        existing.setCloseDay(item.getCloseDay());
        existing.setVetPersonCnt(item.getVetPersonCntAsInteger());
        existing.setSpecsPersonCnt(item.getSpecsPersonCntAsInteger());
        existing.setDataStdDt(item.getParsedDataStdDt());
    }
    
    /**
     * 수동 동기화 트리거
     */
    public void triggerManualSync() {
        log.info("보호소 데이터 수동 동기화 시작");
        syncShelterData();
    }
    
    /**
     * 기존 회원 보호소와 공공데이터 매칭
     * 전화번호 기반으로 매칭 시도
     */
    @Transactional
    public void matchExistingShelters() {
        log.info("기존 회원 보호소와 공공데이터 매칭 시작");
        
        // TODO: user 패키지의 ShelterRepository를 주입받아 처리
        // 이 부분은 순환 참조를 피하기 위해 별도 서비스에서 처리하는 것이 좋음
        
        log.info("보호소 매칭 완료");
    }
}