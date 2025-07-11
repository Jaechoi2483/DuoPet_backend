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
    
    // Entity -> DTO 변환
    public static AdoptionAnimalDto from(AdoptionAnimal entity) {
        return AdoptionAnimalDto.builder()
                .animalId(entity.getAnimalId())
                .shelterId(entity.getShelter() != null ? entity.getShelter().getShelterId() : null)
                .shelterName(entity.getShelter() != null ? entity.getShelter().getShelterName() : null)
                .shelterPhone(entity.getShelter() != null ? entity.getShelter().getPhone() : null)
                .shelterAddress(entity.getShelter() != null ? entity.getShelter().getAddress() : null)
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