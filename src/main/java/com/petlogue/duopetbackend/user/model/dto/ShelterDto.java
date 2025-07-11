package com.petlogue.duopetbackend.user.model.dto;

import com.petlogue.duopetbackend.user.jpa.entity.ShelterEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
public class ShelterDto {

    private Long shelterId;         // 보호소 고유 ID
    private Long userId;            // 연관된 사용자 ID (USERS FK)

    private String shelterName;     // 보호소 이름
    private String phone;           // 보호소 연락처
    private String email;           // 보호소 이메일
    private String address;         // 보호소 주소
    private String website;         // 웹사이트 주소
    private Integer capacity;       // 수용 가능 동물 수 (nullable)
    private String operatingHours;  // 운영 시간

    private String originalFilename;  // 업로드된 파일명
    private String renameFilename;    // 서버 저장용 파일명

    private MultipartFile profileImageFile; // 실제 프로필 이미지 (업로드용)

    // ShelterEntity로 변환 (UserEntity 연관 필수)
    public ShelterEntity toEntity(UserEntity userEntity) {
        return ShelterEntity.builder()
                .user(userEntity)
                .shelterName(this.shelterName)
                .phone(this.phone)
                .email(this.email)
                .address(this.address)
                .website(this.website)
                .capacity(this.capacity)
                .operatingHours(this.operatingHours)
                .originalFilename(this.originalFilename)
                .renameFilename(this.renameFilename)
                .build();
    }
}
