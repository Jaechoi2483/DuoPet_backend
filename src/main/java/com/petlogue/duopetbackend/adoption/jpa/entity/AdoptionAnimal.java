package com.petlogue.duopetbackend.adoption.jpa.entity;

import com.petlogue.duopetbackend.info.jpa.entity.ShelterEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHELTER_ANIMALS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionAnimal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shelter_animals_seq")
    @SequenceGenerator(name = "shelter_animals_seq", sequenceName = "SEQ_SHELTER_ANIMALS", allocationSize = 1)
    @Column(name = "animal_id")
    private Long animalId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id", nullable = true)
    private ShelterEntity shelter;
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "animal_type", length = 30)
    private String animalType; // dog, cat
    
    @Column(name = "breed", length = 50)
    private String breed;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "gender", length = 1)
    private String gender; // M, F
    
    @Column(name = "neutered", length = 1)
    private String neutered; // Y, N
    
    @Column(name = "status", length = 20)
    private String status; // AVAILABLE, PENDING_ADOPTION, NOT_AVAILABLE
    
    @Column(name = "intake_date")
    private LocalDate intakeDate;
    
    @Lob
    @Column(name = "description")
    private String description;
    
    @Column(name = "profile_image")
    private String profileImage;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "rename_filename")
    private String renameFilename;
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    // 공공 API 연동을 위한 추가 필드
    @Column(name = "desertion_no", unique = true)
    private String desertionNo; // 유기번호
    
    @Column(name = "happen_date")
    private LocalDate happenDate; // 발견일
    
    @Column(name = "happen_place")
    private String happenPlace; // 발견장소
    
    @Column(name = "special_mark", length = 1000)
    private String specialMark; // 특징
    
    @Column(name = "public_notice_no")
    private String publicNoticeNo; // 공고번호
    
    @Column(name = "public_notice_start")
    private LocalDate publicNoticeStart; // 공고시작일
    
    @Column(name = "public_notice_end")
    private LocalDate publicNoticeEnd; // 공고종료일
    
    @Column(name = "image_url", length = 500)
    private String imageUrl; // 공공 API 이미지 URL
    
    @Column(name = "api_source", length = 50)
    private String apiSource; // API 출처 구분
    
    @Column(name = "weight")
    private Double weight; // 체중
    
    @Column(name = "color_cd", length = 50)
    private String colorCd; // 색상
    
    @Column(name = "process_state", length = 20)
    private String processState; // 상태 (protect, return, adopt 등)
}