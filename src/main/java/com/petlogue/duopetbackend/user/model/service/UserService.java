package com.petlogue.duopetbackend.user.model.service;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VetService vetService;
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
        userDto.setCreatedAt(LocalDateTime.now());

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

                userDto.setUserProfileOriginalFilename(originalFilename);
                userDto.setUserProfileRenameFilename(renameFilename);
            } catch (Exception e) {
                throw new RuntimeException("파일 저장 중 오류 발생", e);
            }
        }

        return userDto;
    }

    /**
     * [회원가입 4단계] 최종 저장 처리
     * - 프로필 이미지 temp → 정식 저장소로 이동
     * - 비밀번호 암호화
     * - UserEntity 저장
     * - 전문가/보호소일 경우 각 Service 호출
     */
    public UserEntity saveFinalUser(UserDto userDto, MultipartFile file) {

        // 1. 비밀번호 암호화
        if (!userDto.getUserPwd().startsWith("{bcrypt}")) {
            String encodedPwd = passwordEncoder.encode(userDto.getUserPwd());
            userDto.setUserPwd(encodedPwd);
        }

        // 2. 성별 코드 변환
        String gender = userDto.getGender();
        if ("남성".equals(gender)) userDto.setGender("M");
        else if ("여성".equals(gender)) userDto.setGender("F");

        // 3. 프로필 이미지 처리 (temp → real)
        if (userDto.getUserProfileRenameFilename() != null && !userDto.getUserProfileRenameFilename().isEmpty()) {
            String tempPath = "C:/upload_files/user/temp/" + userDto.getUserProfileRenameFilename();
            String realPath = "C:/upload_files/user/" + userDto.getUserProfileRenameFilename();

            File tempFile = new File(tempPath);
            File realFile = new File(realPath);

            if (tempFile.exists()) {
                File realDir = new File("C:/upload_files/user");
                if (!realDir.exists()) realDir.mkdirs();

                boolean success = tempFile.renameTo(realFile);
                if (!success) throw new RuntimeException("프로필 이미지 이동 실패");
            }
        }


        // 4. 상태(status) 설정 (role에 따라 다르게 지정)
        if (userDto.getStatus() == null || userDto.getStatus().isEmpty()) {
            String role = userDto.getRole();
            if (role == null) role = "user";

            switch (role.toLowerCase()) {
                case "vet":
                case "shelter":
                    userDto.setStatus("waiting");
                    break;
                case "user":
                default:
                    userDto.setStatus("active");
                    break;
            }
        } else {
            userDto.setStatus(userDto.getStatus().toLowerCase());
        }

        // 5. 가입일 설정
        if (userDto.getCreatedAt() == null) {
            userDto.setCreatedAt(LocalDateTime.now());
        }

        // 6. 디버깅 로그
        System.out.println("[DEBUG] 최종 저장 전 status: " + userDto.getStatus());
        System.out.println("[DEBUG] 최종 저장 전 role: " + userDto.getRole());

        // 7. USERS 테이블 저장
        UserEntity user = userDto.toEntity();
        userRepository.save(user);
        return user;

        // 전문가(VET) / 보호소(SHELTER) insert는 각 컨트롤러에서 별도로 처리
    }

    public void updateSocialUser(UserDto userDto) {
        // loginId로 기존 유저 조회
        UserEntity user = userRepository.findByLoginId(userDto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        if (!"SOCIAL_TEMP".equals(user.getStatus())) {
            throw new IllegalArgumentException("이미 등록된 사용자입니다.");
        }

        // 추가 정보 입력
        user.setUserName(userDto.getUserName());
        user.setNickname(userDto.getNickname());
        user.setPhone(userDto.getPhone());
        user.setAge(userDto.getAge());
        user.setGender(userDto.getGender());
        user.setAddress(userDto.getAddress());
        user.setStatus("ACTIVE"); // 상태 활성화

        userRepository.save(user);
    }
}
