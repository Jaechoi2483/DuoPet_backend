package com.petlogue.duopetbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로필 이미지 정적 리소스 매핑
        registry.addResourceHandler("/upload/userprofile/**")
                .addResourceLocations("file:///C:/upload_files/userprofile/");
                
        // 다른 업로드 파일들도 필요시 추가
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///C:/upload_files/");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/upload/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3001")
                .allowedMethods("GET")
                .allowCredentials(true);
    }
}