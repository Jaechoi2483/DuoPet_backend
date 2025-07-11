package com.petlogue.duopetbackend.user.jpa.entity;

import com.petlogue.duopetbackend.user.model.dto.ShelterDto;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "shelter")
@Entity
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

    public ShelterDto toDto() {
        return ShelterDto.builder()
                .shelterId(shelterId)
                .userId(user != null ? user.getUserId() : null)
                .shelterName(shelterName)
                .phone(phone)
                .email(email)
                .address(address)
                .website(website)
                .capacity(capacity)
                .operatingHours(operatingHours)
                .renameFilename(renameFilename)
                .originalFilename(originalFilename)
                .build();
    }
}
