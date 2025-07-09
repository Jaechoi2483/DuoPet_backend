package com.petlogue.duopetbackend.user.model.service;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리 (1단계)
     * - 아이디 중복 검사
     * - 비밀번호 암호화
     * - 기본값 설정 (role, status, createdAt)
     * - DB 저장
     */
    public void signup(UserDto userDto) {

        // 1. 아이디 중복 검사
        if (userRepository.existsByLoginId(userDto.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPwd = passwordEncoder.encode(userDto.getUserPwd());
        userDto.setUserPwd(encodedPwd);

        // 3. 기본값 설정
        userDto.setRole("USER");
        userDto.setStatus("ACTIVE");
        userDto.setCreatedAt(new Date());

        // 4. Entity 변환 후 저장
        UserEntity user = userDto.toEntity();
        userRepository.save(user);
    }
}
