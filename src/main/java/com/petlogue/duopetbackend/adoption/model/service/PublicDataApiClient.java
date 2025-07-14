package com.petlogue.duopetbackend.adoption.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicDataApiClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${api.animal.protection.service-key}")
    private String shelterServiceKey;
    
    @Value("${api.animal.protection.base-url}")
    private String shelterBaseUrl;
    
    @Value("${api.animal.registration.service-key}")
    private String animalServiceKey;
    
    @Value("${api.animal.registration.base-url}")
    private String animalBaseUrl;
    
    /**
     * 보호소 정보 조회
     * @param page 페이지 번호
     * @param numOfRows 한 페이지 결과 수
     * @return 보호소 정보 리스트
     */
    public Map<String, Object> getShelterInfo(int page, int numOfRows) {
        URI uri = UriComponentsBuilder.fromUriString(shelterBaseUrl)
                .queryParam("serviceKey", shelterServiceKey)
                .queryParam("pageNo", page)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", "json")
                .build()
                .encode()
                .toUri();
        
        log.info("Calling Shelter API: {}", uri);
        
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
    
    /**
     * 유기동물 정보 조회
     * @param bgnde 유기날짜 시작일 (YYYYMMDD)
     * @param endde 유기날짜 종료일 (YYYYMMDD)
     * @param upkind 축종코드 (개: 417000, 고양이: 422400, 기타: 429900)
     * @param state 상태 (전체: null, 공고중: notice, 보호중: protect)
     * @param pageNo 페이지 번호
     * @param numOfRows 한 페이지 결과 수
     * @param uprCd 시도코드 (경기도: 6410000)
     * @return 유기동물 정보
     */
    public Map<String, Object> getAbandonmentAnimals(
            String bgnde, String endde, String upkind, 
            String state, int pageNo, int numOfRows, String uprCd) {
        
        try {
            // 서비스 키가 이미 디코딩된 상태이므로 직접 사용
            // 공공 API는 URL 인코딩된 키를 요구하므로 인코딩
            String encodedServiceKey = URLEncoder.encode(animalServiceKey, StandardCharsets.UTF_8);
            
            // 직접 URL 구성
            String url = animalBaseUrl + "?serviceKey=" + encodedServiceKey;
            url += "&pageNo=" + pageNo;
            url += "&numOfRows=" + numOfRows;
            url += "&_type=json";
            
            if (bgnde != null) url += "&bgnde=" + bgnde;
            if (endde != null) url += "&endde=" + endde;
            if (upkind != null) url += "&upkind=" + upkind;
            if (state != null) url += "&state=" + state;
            if (uprCd != null) url += "&upr_cd=" + uprCd;
            
            log.info("Calling Animal API with URL: {}", url);
            
            // Java 11 HttpClient 사용
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
                    
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            String response = httpResponse.body();
            
            log.info("Response status: {}", httpResponse.statusCode());
                    
            log.info("API Response (first 200 chars): {}", 
                response.substring(0, Math.min(200, response.length())));
                
            // JSON 파싱
            if (response.trim().startsWith("<")) {
                log.error("API returned XML. URL was: {}", url);
                log.error("Service key length: {}", animalServiceKey.length());
                log.error("Encoded service key length: {}", encodedServiceKey.length());
                throw new RuntimeException("API returned XML. Response: " + response.substring(0, 200) + "\nURL: " + url);
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(response, Map.class);
                    
        } catch (Exception e) {
            log.error("Error calling Animal API", e);
            throw new RuntimeException("Failed to call Animal API: " + e.getMessage(), e);
        }
    }
    
    public int getServiceKeyLength() {
        return animalServiceKey != null ? animalServiceKey.length() : 0;
    }
    
    public String getBaseUrl() {
        return animalBaseUrl;
    }
    
    /**
     * 특정 보호소의 보호동물 조회
     * @param careRegNo 보호소 등록번호
     * @param pageNo 페이지 번호
     * @param numOfRows 한 페이지 결과 수
     * @return 보호동물 정보
     */
    public Map<String, Object> getAnimalsByShelter(String careRegNo, int pageNo, int numOfRows) {
        URI uri = UriComponentsBuilder.fromUriString(animalBaseUrl)
                .queryParam("serviceKey", animalServiceKey)
                .queryParam("care_reg_no", careRegNo)
                .queryParam("state", "protect")
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", "json")
                .build()
                .encode()
                .toUri();
        
        log.info("Calling Animals by Shelter API: {}", uri);
        
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}