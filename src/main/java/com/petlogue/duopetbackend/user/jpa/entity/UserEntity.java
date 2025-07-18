package com.petlogue.duopetbackend.user.jpa.entity;

import com.petlogue.duopetbackend.user.model.dto.UserDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.GregorianCalendar;

@Table(name = "users")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Oracle이면 sequence 사용 가능
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;
    @Column(name = "user_pwd", nullable = false)
    private String userPwd;
    @Column(name = "provider")
    private String provider;
    @Column(name = "provider_id")
    private String providerId;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "nickname")
    private String nickname;
    @Column(name = "phone")
    private String phone;
    @Column(name = "age")
    private Integer age;
    @Column(name = "gender", length = 1)
    private String gender;
    @Column(name = "address")
    private String address;
    @Column(name = "user_email")
    private String userEmail;
    @Column(name = "role", nullable = false)
    private String role;
    @Column(name = "status")
    private String status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "rename_filename")
    private String renameFilename;
    @Column(name = "original_filename")
    private String originalFilename;
    @Column(name = "face_recognition_id")
    private String faceRecognitionId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();    //현재 날짜 시간 적용
    }

    public UserDto toDto(){
        return UserDto.builder()
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
                .userProfileOriginalFilename(originalFilename)
                .userProfileRenameFilename(renameFilename)
                .faceRecognitionId(faceRecognitionId)
                .build();
    }
}
