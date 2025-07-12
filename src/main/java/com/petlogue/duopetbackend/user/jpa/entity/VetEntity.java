package com.petlogue.duopetbackend.user.jpa.entity;

import com.petlogue.duopetbackend.user.model.dto.VetDto;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "vet")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vet_id")
    private Long vetId;

    // 연관 사용자 (FK: users.user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "website")
    private String website;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "rename_filename")
    private String renameFilename;

    @Column(name = "original_filename")
    private String originalFilename;

    // DTO 변환
    public VetDto toDto() {
        return VetDto.builder()
                .vetId(vetId)
                .userId(user != null ? user.getUserId() : null)
                .name(name)
                .licenseNumber(licenseNumber)
                .phone(phone)
                .email(email)
                .address(address)
                .website(website)
                .specialization(specialization)
                .vetFileOriginalFilename(originalFilename)
                .vetFileRenameFilename(renameFilename)
                .build();
    }
}
