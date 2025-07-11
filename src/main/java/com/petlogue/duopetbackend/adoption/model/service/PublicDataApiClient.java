package com.petlogue.duopetbackend.adoption.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
     * @return 유기동물 정보
     */
    public Map<String, Object> getAbandonmentAnimals(
            String bgnde, String endde, String upkind, 
            String state, int pageNo, int numOfRows) {
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(animalBaseUrl)
                .queryParam("serviceKey", animalServiceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", "json");
        
        if (bgnde != null) builder.queryParam("bgnde", bgnde);
        if (endde != null) builder.queryParam("endde", endde);
        if (upkind != null) builder.queryParam("upkind", upkind);
        if (state != null) builder.queryParam("state", state);
        
        URI uri = builder.build().encode().toUri();
        
        log.info("Calling Animal API: {}", uri);
        
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
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