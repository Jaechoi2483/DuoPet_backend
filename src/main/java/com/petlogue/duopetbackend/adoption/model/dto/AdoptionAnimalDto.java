package com.petlogue.duopetbackend.adoption.model.dto;

import com.petlogue.duopetbackend.adoption.jpa.entity.AdoptionAnimal;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionAnimalDto {
    
    private Long animalId;
    private Long shelterId;
    private String shelterName;
    private String shelterPhone;
    private String shelterAddress;
    private String orgNm; // 관할기관
    
    // 기본 정보
    private String name;
    private String animalType;
    private String breed;
    private Integer age;
    private String gender;
    private String neutered;
    private String status;
    private LocalDate intakeDate;
    private String description;
    private String profileImage;
    
    // 공공 API 정보
    private String desertionNo;
    private LocalDate happenDate;
    private String happenPlace;
    private String specialMark;
    private String publicNoticeNo;
    private LocalDate publicNoticeStart;
    private LocalDate publicNoticeEnd;
    private String imageUrl;
    private Double weight;
    private String colorCd;
    private String processState;
    
    /**
     * 표시용 이미지 URL 반환 (fallback 처리 포함)
     * @return 이미지 URL 또는 기본 이미지 경로
     */
    public String getDisplayImageUrl() {
        // 우선순위: imageUrl -> profileImage -> 기본 이미지
        if (this.imageUrl != null && !this.imageUrl.trim().isEmpty()) {
            return this.imageUrl;
        } else if (this.profileImage != null && !this.profileImage.trim().isEmpty()) {
            return this.profileImage;
        } else {
            // 동물 종류에 따른 기본 이미지
            if ("개".equals(this.animalType) || "dog".equalsIgnoreCase(this.animalType)) {
                return "/images/default-dog.png";
            } else if ("고양이".equals(this.animalType) || "cat".equalsIgnoreCase(this.animalType)) {
                return "/images/default-cat.png";
            } else {
                return "/images/default-animal.png";
            }
        }
    }
    
    // Entity -> DTO 변환
    public static AdoptionAnimalDto from(AdoptionAnimal entity) {
        return AdoptionAnimalDto.builder()
                .animalId(entity.getAnimalId())
                .shelterId(entity.getShelter() != null ? entity.getShelter().getShelterId() : null)
                .shelterName(entity.getShelter() != null ? entity.getShelter().getShelterName() : entity.getApiShelterName())
                .shelterPhone(entity.getShelter() != null ? entity.getShelter().getPhone() : entity.getApiShelterTel())
                .shelterAddress(entity.getShelter() != null ? entity.getShelter().getAddress() : entity.getApiShelterAddr())
                .orgNm(entity.getApiOrgNm())
                .name(entity.getName())
                .animalType(entity.getAnimalType())
                .breed(entity.getBreed())
                .age(entity.getAge())
                .gender(entity.getGender())
                .neutered(entity.getNeutered())
                .status(entity.getStatus())
                .intakeDate(entity.getIntakeDate())
                .description(entity.getDescription())
                .profileImage(entity.getProfileImage())
                .desertionNo(entity.getDesertionNo())
                .happenDate(entity.getHappenDate())
                .happenPlace(entity.getHappenPlace())
                .specialMark(entity.getSpecialMark())
                .publicNoticeNo(entity.getPublicNoticeNo())
                .publicNoticeStart(entity.getPublicNoticeStart())
                .publicNoticeEnd(entity.getPublicNoticeEnd())
                .imageUrl(entity.getImageUrl())
                .weight(entity.getWeight())
                .colorCd(entity.getColorCd())
                .processState(entity.getProcessState())
                .build();
    }
}