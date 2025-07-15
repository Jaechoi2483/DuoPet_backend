package com.petlogue.duopetbackend.user.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
@Builder
public class UserDto {

    private Long userId; // NUMBER - 회원 고유번호

    @NotBlank
    private String loginId; // 로그인 ID
    @NotBlank
    private String userPwd; // 로그인 비밀번호
    private String provider;    // 소셜로그인 제공자 (KAKAO 등)
    private String providerId;  // 소셜로그인 고유 ID
    private String userName;    // 실명
    private String nickname;    // 닉네임
    private String phone;       // 전화번호
    private Integer age;        // 나이
    private String gender;      // 성별 (M / F)
    private String address;     // 주소
    @Email
    private String userEmail;   // 이메일
    private String role;        // USER / ADMIN / VET / SHELTER
    private String status;      // ACTIVE / INACTIVE / SUSPENDED
    private LocalDateTime createdAt;     // 가입일
    private String userProfileRenameFilename;      // 서버 저장용 프로필 파일명
    private String userProfileOriginalFilename;    // 원본 파일명
    private String faceRecognitionId;   // 얼굴 인식 ID

    private VetDto vetProfile;
    private ShelterDto shelterProfile;

    public UserEntity toEntity() {
        return UserEntity.builder()
                .userId(userId)
                .loginId(loginId)
                .userPwd(userPwd)
                .provider(provider)
                .providerId(providerId)
                .userName(userName)
                .nickname(nickname)
                .phone(phone)
                .age(age)
                .gender(gender)
                .address(address)
                .userEmail(userEmail)
                .role(role)
                .status(status)
                .createdAt(createdAt)
                .renameFilename(userProfileRenameFilename)
                .originalFilename(userProfileOriginalFilename)
                .faceRecognitionId(faceRecognitionId)
                .build();
    }
}