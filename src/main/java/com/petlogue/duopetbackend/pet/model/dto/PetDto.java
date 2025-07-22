// src/main/java/com/petlogue/duopetbackend/pet/model/dto/PetDto.java

package com.petlogue.duopetbackend.pet.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetDto {

    private Long petId;                // 반려동물 ID (PK)
    private Long userId;                // 사용자 ID (FK)
    private String petName;            // 이름
    private String animalType;         // 종 (강아지/고양이)
    private String breed;              // 품종 (선택)
    private Integer age;               // 나이
    private String gender;             // M / F
    private String neutered;           // Y / N
    private Double weight;             // 체중
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate registrationDate; // 등록일
    
    // 파일 처리용
    private MultipartFile file;        // 첨부 이미지
    private String originalFilename;   // 원본 파일명
    private String renameFilename;     // 서버 저장용 파일명
    private String imageUrl;           // 이미지 URL (조회시 사용)

    public PetEntity toEntity(UserEntity user) {
        return PetEntity.builder()
                .user(user)
                .petName(petName)
                .animalType(animalType)
                .breed(breed)
                .age(age)
                .gender(gender)
                .neutered(neutered)
                .weight(weight)
                .registrationDate(LocalDate.now()) // 등록일은 현재 날짜
                .originalFilename(originalFilename)
                .renameFilename(renameFilename)
                .build();
    }
    
    // Entity를 DTO로 변환하는 정적 메서드
    public static PetDto fromEntity(PetEntity entity) {
        return PetDto.builder()
                .petId(entity.getPetId())
                .userId(entity.getUser().getUserId())
                .petName(entity.getPetName())
                .animalType(entity.getAnimalType())
                .breed(entity.getBreed())
                .age(entity.getAge())
                .gender(entity.getGender())
                .neutered(entity.getNeutered())
                .weight(entity.getWeight())
                .registrationDate(entity.getRegistrationDate())
                .originalFilename(entity.getOriginalFilename())
                .renameFilename(entity.getRenameFilename())
                .imageUrl(entity.getRenameFilename() != null ? "/pet/image/" + entity.getRenameFilename() : null)
                .build();
    }

}