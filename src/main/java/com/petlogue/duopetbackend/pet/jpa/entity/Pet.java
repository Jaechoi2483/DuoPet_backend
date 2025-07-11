package com.petlogue.duopetbackend.pet.jpa.entity;

import com.petlogue.duopetbackend.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pet")
@Getter
@Setter
@NoArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long petId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pet_name", nullable = false, length = 50)
    private String petName;

    @Column(name = "animal_type", nullable = false, length = 30)
    private String animalType;

    @Column(name = "breed", length = 50)
    private String breed;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender", nullable = false, length = 1)
    private String gender;

    @Column(name = "neutered", nullable = false, length = 1)
    private String neutered;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "rename_filename", length = 255)
    private String renameFilename;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;
}
