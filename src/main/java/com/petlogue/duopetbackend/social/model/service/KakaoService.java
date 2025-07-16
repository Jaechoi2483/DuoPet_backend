package com.petlogue.duopetbackend.social.model.service;

import com.petlogue.duopetbackend.social.model.dto.KakaoUserDto;
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
public class KakaoService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        KakaoUserDto kakaoUser = new KakaoUserDto(oAuth2User.getAttributes());

        String loginId = "kakao_" + kakaoUser.getId();
        Optional<UserEntity> existingUser = userRepository.findByLoginId(loginId);

        boolean isNewUser = existingUser.isEmpty();

        if (isNewUser) {
            log.info("소셜 로그인 최초 사용자 → 임시 계정 저장: {}", loginId);

            UserEntity userEntity = UserEntity.builder()
                    .loginId(loginId)  // 예: "kakao_4350076087"
                    .provider("KAKAO")  // 소셜 로그인 제공자 (예: "KAKAO")
                    .providerId(kakaoUser.getId())  // 제공자에서 받은 고유 ID (예: "4350076087")
                    .userName(kakaoUser.getNickname())  // 여기를 nickname으로 임시 지정!
                    .nickname(kakaoUser.getNickname())
                    .userEmail(kakaoUser.getEmail())
                    .phone("소셜가입")
                    .role("user")
                    .status("social_temp")
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(userEntity);

        } else {
            log.info("소셜 로그인 기존 사용자: {}", loginId);
        }

        // 임시로 리다이렉션 URL 설정 (Spring Config에 설정 필요)
        String redirectUri = "http://localhost:3000/social-redirect";
        String accessToken = "임시토큰"; // 이후 OAuth2SuccessHandler에서 실제 토큰 발급
        String refreshToken = "임시리프레시"; // 이후 OAuth2SuccessHandler에서 발급

        // isNew 값 전달
        String targetUrl = redirectUri + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken + "&isNew=" + isNewUser;

        // 실제 리디렉션은 OAuth2SuccessHandler에서 수행하므로 여기선 user 반환
        return oAuth2User;
    }
}
