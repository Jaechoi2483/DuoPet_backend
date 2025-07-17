package com.petlogue.duopetbackend.social.model.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class NaverUserDto {

    private String id;       // 네이버 고유 ID
    private String email;    // 네이버 이메일
    private String name;     // 사용자 이름

    public NaverUserDto(Map<String, Object> attributes) {
        // 네이버는 사용자 정보가 "response" 안에 들어 있음
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        this.id = response.get("id").toString();
        this.email = response.get("email") != null ? response.get("email").toString() : null;
        this.name = response.get("name") != null ? response.get("name").toString() : "네이버사용자";
    }
}
