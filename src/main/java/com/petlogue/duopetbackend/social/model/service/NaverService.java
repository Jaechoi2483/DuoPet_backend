package com.petlogue.duopetbackend.social.model.service;

import com.petlogue.duopetbackend.social.model.dto.NaverUserDto;
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
public class NaverService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        NaverUserDto naverUser = new NaverUserDto(oAuth2User.getAttributes());

        String loginId = "naver_" + naverUser.getId();
        Optional<UserEntity> existingUser = userRepository.findByLoginId(loginId);

        boolean isNewUser = existingUser.isEmpty();

        if (isNewUser) {
            log.info("네이버 소셜 로그인 최초 사용자 → 임시 계정 저장: {}", loginId);

            UserEntity userEntity = UserEntity.builder()
                    .loginId(loginId)
                    .provider("NAVER")
                    .providerId(naverUser.getId())
                    .userName(naverUser.getName())
                    .nickname(naverUser.getName())
                    .userEmail(naverUser.getEmail())
                    .phone("소셜가입")
                    .role("user")
                    .status("social_temp")
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(userEntity);

        } else {
            log.info("네이버 기존 사용자 로그인: {}", loginId);
        }

        // 리디렉션과 토큰 발급은 SuccessHandler에서 수행
        return oAuth2User;
    }
}
