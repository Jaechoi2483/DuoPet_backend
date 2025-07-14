package com.petlogue.duopetbackend.pet.jpa.repository;

import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.pet.jpa.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

//  관리자 페이지 통게 표시용 이에요
    @Query("SELECT new com.petlogue.duopetbackend.admin.model.dto.StatItemDto(p.animalType, COUNT(p)) FROM Pet p GROUP BY p.animalType")
    List<StatItemDto> findAnimalTypeStat();

    @Query("SELECT new com.petlogue.duopetbackend.admin.model.dto.StatItemDto(p.neutered, COUNT(p)) FROM Pet p GROUP BY p.neutered")
    List<StatItemDto> findNeuteredStat();


}