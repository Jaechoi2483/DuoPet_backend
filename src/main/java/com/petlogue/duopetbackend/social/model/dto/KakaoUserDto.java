package com.petlogue.duopetbackend.social.model.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoUserDto {
    private String id;
    private String email;
    private String nickname;

    public KakaoUserDto(Map<String, Object> attributes) {
        this.id = attributes.get("id").toString();

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.email = kakaoAccount.containsKey("email") ? (String) kakaoAccount.get("email") : null;

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        this.nickname = profile.containsKey("nickname") ? (String) profile.get("nickname") : null;
    }
}
