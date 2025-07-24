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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

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

    // 사용자 정보 업데이트
    public void updateUserInfo(Long userId, UserDto userDto) {
        // 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 전체 사용자 정보 업데이트
        user.setPhone(userDto.getPhone());
        user.setAge(userDto.getAge());
        user.setGender(userDto.getGender());
        user.setAddress(userDto.getAddress());
        user.setUserEmail(userDto.getUserEmail());
        user.setNickname(userDto.getNickname());

        // 상태 변경 (소셜 로그인 후 첫 가입이므로 'active'로 변경)
        user.setStatus("active");

        // 업데이트된 사용자 저장
        userRepository.save(user);
    }

    // 사용자 정보 조회
    public UserEntity getUserByUserId(Long userId) {
        return userRepository.findByUserId(userId);  // DB에서 userId로 사용자 정보 조회
    }

    /**
     * 사용자 프로필 업데이트 (프로필 이미지 포함)
     */
    public UserEntity updateUserProfile(Long userId, UserDto userDto, MultipartFile file) {
        // 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 사용자 정보 업데이트
        if (userDto.getUserName() != null) user.setUserName(userDto.getUserName());
        if (userDto.getUserEmail() != null) user.setUserEmail(userDto.getUserEmail());
        if (userDto.getPhone() != null) user.setPhone(userDto.getPhone());
        if (userDto.getAddress() != null) user.setAddress(userDto.getAddress());
        
        // 프로필 이미지 처리
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String renameFilename = FileNameChange.change(originalFilename, "yyyyMMddHHmmss");

                String uploadDir = "C:/upload_files/userprofile";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                // 기존 프로필 이미지 삭제
                if (user.getRenameFilename() != null) {
                    File oldFile = new File(uploadDir, user.getRenameFilename());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // 새 프로필 이미지 저장
                File dest = new File(dir, renameFilename);
                file.transferTo(dest);

                user.setOriginalFilename(originalFilename);
                user.setRenameFilename(renameFilename);
            } catch (Exception e) {
                throw new RuntimeException("프로필 이미지 저장 중 오류 발생", e);
            }
        }

        // 업데이트된 사용자 저장
        return userRepository.save(user);
    }

    /**
     * [아이디 찾기] 이름 + 전화번호로 loginId 조회
     */
    public String findLoginIdByNameAndPhone(String userName, String phone) {
        if (userName == null || phone == null) {
            throw new IllegalArgumentException("이름과 전화번호는 필수입니다.");
        }

        String trimmedName = userName.trim();
        String normalizedPhone = phone.replaceAll("-", "").trim();

        return userRepository.findByUserNameAndPhoneWithoutHyphen(trimmedName, normalizedPhone)
                .map(UserEntity::getLoginId)
                .orElseThrow(() -> new RuntimeException("일치하는 사용자를 찾을 수 없습니다."));
    }

    /**
     * [비밀번호 찾기] loginId + 전화번호가 일치하는 사용자가 존재하는지 확인
     */
    public boolean existsByLoginIdAndPhone(String loginId, String phone) {
        String normalizedPhone = phone.replaceAll("-", "");

        Optional<UserEntity> user = userRepository.findByLoginId(loginId);
        return user
                .map(u -> {
                    String userPhone = u.getPhone();
                    return userPhone != null &&
                            userPhone.replaceAll("-", "").equals(normalizedPhone);
                })
                .orElse(false);
    }

    /**
     * 비밀번호 재설정
     */
    public void resetPassword(String loginId, String newPassword) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자를 찾을 수 없습니다."));

        // 회원가입과 동일한 방식의 조건부 암호화 적용
        if (!newPassword.startsWith("{bcrypt}")) {
            String encodedPwd = passwordEncoder.encode(newPassword);
            newPassword = encodedPwd;
        }

        user.setUserPwd(newPassword);
        userRepository.save(user);
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("-", "");
    }

    /**
     * 얼굴 이미지 저장 처리
     * - 얼굴 이미지 업로드 시 서버에 저장하고, 파일명을 DB에 반영한다.
     * - 등록 여부는 faceOriginalFilename / faceRenameFilename 존재 여부로 판단한다.
     */
    public String saveFaceImageByLoginId(Long userId, MultipartFile faceImage) {
        // 1. 사용자 조회
        UserEntity entity = userRepository.findByUserId(userId);
        if (entity == null) {
            throw new RuntimeException("해당 사용자를 찾을 수 없습니다: " + userId);
        }

        // 2. 파일 유효성 검사
        if (faceImage == null || faceImage.isEmpty()) {
            throw new RuntimeException("업로드할 얼굴 이미지가 비어있습니다.");
        }

        try {
            // 3-1. 기존 파일 삭제 로직 추가 (여기만 새로 추가하면 됨)
            String existingFilename = entity.getFaceRenameFilename();
            if (existingFilename != null) {
                String baseDir = "C:/upload_files/face/";
                File oldImage = new File(baseDir + existingFilename);
                File oldEmbedding = new File(baseDir + existingFilename + ".npy");

                if (oldImage.exists()) oldImage.delete();
                if (oldEmbedding.exists()) oldEmbedding.delete();
            }

            // 3. 파일명 처리
            String originalName = faceImage.getOriginalFilename(); // 예: "face.png"
            String userAwareOriginal = userId + "_" + originalName;
            String rename = FileNameChange.change(originalName, "'face_'yyyyMMdd_HHmmss"); // 예: face_20250721_211000.png

            // 4. 저장 경로 설정
            String savePath = "C:/upload_files/face/" + rename;
            File file = new File(savePath);

            // 5. 디렉토리 없을 경우 생성
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // 6. 실제 파일 저장
            faceImage.transferTo(file);

            // 7. DB에 파일명 저장 (등록 여부 판단 기준)
            entity.setFaceOriginalFilename(userAwareOriginal);
            entity.setFaceRenameFilename(rename);

            // 8. 사용자 정보 저장
            userRepository.save(entity);
            return rename;

        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException("얼굴 이미지 저장 중 오류 발생", e);
        }
    }

    /**
     * 얼굴 등록 여부 확인.
     * FACE_ORIGINAL_FILENAME, FACE_RENAME_FILENAME 둘 다 값이 있을 때 true.
     */
    @Transactional(readOnly = true)
    public boolean isFaceRegistered(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getFaceOriginalFilename() != null && u.getFaceRenameFilename() != null)
                .orElse(false);
    }
}
