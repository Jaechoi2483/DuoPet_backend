package com.petlogue.duopetbackend.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * RestTemplate 및 ObjectMapper 설정
 * 공공데이터 API 호출 등에 사용
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * RestTemplate Bean 설정
     * - 타임아웃 설정
     * - JSON 변환기 설정
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        return builder
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .additionalMessageConverters(
                    new StringHttpMessageConverter(StandardCharsets.UTF_8),
                    mappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }
    
    /**
     * ObjectMapper Bean 설정
     * - Java Time 모듈 등록 (LocalDate, LocalDateTime 처리)
     * - 알 수 없는 속성 무시
     * - NULL 값 처리
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Java 8 날짜/시간 타입 지원
        objectMapper.registerModule(new JavaTimeModule());
        
        // JSON에 있지만 DTO에 없는 필드 무시
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 빈 문자열을 null로 처리
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        return objectMapper;
    }
    
    /**
     * JSON 메시지 변환기
     */
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}