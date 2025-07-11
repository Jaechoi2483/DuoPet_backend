package com.petlogue.duopetbackend.user.model.service;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    /**
     * [회원가입 2단계]
     * - 닉네임 중복 검사
     * - 성별 코드 변환 ("남성"/"여성" → "M"/"F")
     * - 프로필 이미지 temp 디렉토리에 저장
     * - DB 저장은 아직 하지 않음
     */
    public UserDto processStep2(UserDto userDto, MultipartFile file) {
        if (userRepository.existsByNickname(userDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String gender = userDto.getGender();
        if ("남성".equals(gender)) {
            userDto.setGender("M");
        } else if ("여성".equals(gender)) {
            userDto.setGender("F");
        } else {
            throw new IllegalArgumentException("성별 값이 유효하지 않습니다.");
        }

        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String renameFilename = FileNameChange.change(originalFilename, "yyyyMMddHHmmss");

                String uploadDir = "C:/upload_files/user/temp";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                File dest = new File(dir, renameFilename);
                file.transferTo(dest);

                userDto.setOriginalFilename(originalFilename);
                userDto.setRenameFilename(renameFilename);
            } catch (Exception e) {
                throw new RuntimeException("파일 저장 중 오류 발생", e);
            }
        }

        return userDto;
    }

}
