package com.petlogue.duopetbackend.security.model.service;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없습니다: {}", loginId);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
                });

        log.info("DB에서 조회된 사용자: {}", user.getLoginId());
        log.info("암호화된 비밀번호: {}", user.getUserPwd());  // 이 줄 추가해서 값 확인

        return User.builder()
                .username(user.getLoginId())
                .password(user.getUserPwd())
                .roles(user.getRole() != null ? user.getRole().toUpperCase() : "USER")
                .build();
    }
}
