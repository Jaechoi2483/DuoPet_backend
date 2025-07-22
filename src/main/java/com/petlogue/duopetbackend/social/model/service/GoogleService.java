package com.petlogue.duopetbackend.social.model.service;

import com.petlogue.duopetbackend.social.model.dto.GoogleUserDto;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        GoogleUserDto googleUser = new GoogleUserDto(oAuth2User.getAttributes());

        String loginId = "google_" + googleUser.getId();
        Optional<UserEntity> existingUser = userRepository.findByLoginId(loginId);

        boolean isNewUser = existingUser.isEmpty();

        if (isNewUser) {
            log.info("소셜 로그인 최초 사용자 → 임시 계정 저장: {}", loginId);

            UserEntity user = UserEntity.builder()
                    .loginId(loginId)
                    .provider("GOOGLE")
                    .providerId(googleUser.getId())
                    .userName(googleUser.getName())
                    .nickname(loginId)
                    .userEmail(googleUser.getEmail())
                    .phone("소셜가입")
                    .role("user")
                    .status("social_temp")
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);
        } else {
            log.info("소셜 로그인 기존 사용자: {}", loginId);
        }

        return oAuth2User;
    }
}