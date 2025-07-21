package com.petlogue.duopetbackend.pet.jpa.repository;

import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;

import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<PetEntity, Long> {

//  관리자 페이지 통게 표시용 이에요
    @Query("SELECT new com.petlogue.duopetbackend.admin.model.dto.StatItemDto(p.animalType, COUNT(p)) FROM PetEntity p GROUP BY p.animalType")
    List<StatItemDto> findAnimalTypeStat();

    @Query("SELECT new com.petlogue.duopetbackend.admin.model.dto.StatItemDto(p.neutered, COUNT(p)) FROM PetEntity p GROUP BY p.neutered")
    List<StatItemDto> findNeuteredStat();
    
    // 사용자별 반려동물 목록 조회
    List<PetEntity> findByUser(UserEntity user);
    
    // 사용자 ID로 반려동물 목록 조회
    List<PetEntity> findByUser_UserId(Long userId);
}