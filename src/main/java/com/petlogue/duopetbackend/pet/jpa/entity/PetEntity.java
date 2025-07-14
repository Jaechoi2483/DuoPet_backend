// src/main/java/com/petlogue/duopetbackend/pet/jpa/entity/PetEntity.java

package com.petlogue.duopetbackend.pet.jpa.entity;

import com.petlogue.duopetbackend.pet.model.dto.PetDto;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "PET")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long petId;

    // 사용자 정보 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "pet_name", nullable = false)
    private String petName;

    @Column(name = "animal_type", nullable = false)
    private String animalType;

    @Column(name = "breed")
    private String breed;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender", nullable = false)
    private String gender;  // M / F

    @Column(name = "neutered", nullable = false)
    private String neutered;  // Y / N

    @Column(name = "weight")
    private Double weight;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "rename_filename")
    private String renameFilename;

    @Column(name = "original_filename")
    private String originalFilename;

    public PetDto toDto() {
        return PetDto.builder()
                .userId(user != null ? user.getUserId() : null)
                .petName(petName)
                .animalType(animalType)
                .breed(breed)
                .age(age)
                .gender(gender)
                .neutered(neutered)
                .weight(weight)
                .originalFilename(originalFilename)
                .renameFilename(renameFilename)
                .build();
    }
}
