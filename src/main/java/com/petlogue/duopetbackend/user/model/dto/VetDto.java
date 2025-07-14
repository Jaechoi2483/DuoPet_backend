package com.petlogue.duopetbackend.user.model.dto;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
public class VetDto {

    private Long vetId; // 전문가 고유 식별자
    private Long userId; // USERS 테이블의 user_id 참조
    @NotBlank
    private String name; // 전문가 이름 (UserDto.userName과 동일 가능)
    @NotBlank
    private String licenseNumber; // 수의사 면허번호
    private String phone;   // 전문가 연락처 (UserDto.phone 사용 가능)
    private String email;   // 전문가 이메일 (UserDto.userEmail 사용 가능)
    private String address; // 병원 주소
    private String website; // 병원 웹사이트 주소
    private String specialization; // 전문 분야
    private String vetFileOriginalFilename; // 면허증 원본 파일명
    private String vetFileRenameFilename;   // 저장된 파일명
    private MultipartFile licenseFile; // 실제 업로드된 면허증 파일 (DB 미저장)

    public VetEntity toEntity(UserEntity userEntity) {
        return VetEntity.builder()
                .user(userEntity)
                .name(this.name)
                .licenseNumber(this.licenseNumber)
                .phone(this.phone)
                .email(this.email)
                .address(this.address)
                .website(this.website)
                .specialization(this.specialization)
                .originalFilename(this.vetFileOriginalFilename)
                .renameFilename(this.vetFileRenameFilename)
                .build();
    }
}
