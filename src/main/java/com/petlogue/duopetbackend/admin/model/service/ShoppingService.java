package com.petlogue.duopetbackend.admin.model.service;

import com.petlogue.duopetbackend.admin.model.dto.ShoppingResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class ShoppingService {

    @Value("${naver.api.client-id}")
    private String naverApiClientId;

    @Value("${naver.api.client-secret}")
    private String naverApiClientSecret;

    public ShoppingResponseDto search(String query, Integer display, Integer start) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverApiClientId);
        headers.set("X-Naver-Client-Secret", naverApiClientSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/shop.json")
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("start", start)
                .queryParam("sort", "sim") // sim(유사도순), date(날짜순), asc(가격 오름차순), dsc(가격 내림차순)
                .encode()
                .build()
                .toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, entity, ShoppingResponseDto.class).getBody();
    }

}
