package com.petlogue.duopetbackend.user.jpa.entity;

import com.petlogue.duopetbackend.info.jpa.entity.ShelterInfo;
import com.petlogue.duopetbackend.user.model.dto.ShelterDto;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "shelter")
@Entity(name = "UserShelterEntity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShelterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelter_id")
    private Long shelterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "shelter_name", nullable = false)
    private String shelterName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "website")
    private String website;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "rename_filename")
    private String renameFilename;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "auth_file_description")
    private String authFileDescription;

    // 공공데이터 보호소 정보와의 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_info_id")
    private ShelterInfo shelterInfo;

    public ShelterDto toDto() {
        // shelter_info_id가 있으면 공공데이터에서 정보를 가져옴
        String finalShelterName = shelterName;
        String finalPhone = phone;
        String finalAddress = address;
        
        if (shelterInfo != null) {
            // 공공데이터가 있으면 그 정보를 우선 사용
            finalShelterName = shelterInfo.getCareNm();
            finalPhone = shelterInfo.getCareTel();
            finalAddress = shelterInfo.getCareAddr();
        }
        
        return ShelterDto.builder()
                .shelterId(shelterId)
                .userId(user != null ? user.getUserId() : null)
                .shelterName(finalShelterName)
                .phone(finalPhone)
                .email(email)
                .address(finalAddress)
                .website(website)
                .capacity(capacity)
                .operatingHours(operatingHours)
                .shelterFileOriginalFilename(originalFilename)
                .shelterFileRenameFilename(renameFilename)
                .authFileDescription(authFileDescription)
                .shelterInfoId(shelterInfo != null ? shelterInfo.getShelterInfoId() : null)
                .build();
    }
}
