package com.petlogue.duopetbackend.health.jpa.repository;

import com.petlogue.duopetbackend.health.jpa.entity.HealthRecordEntity;
import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecordEntity, Long> {
    
    // 특정 반려동물의 건강 기록 조회
    List<HealthRecordEntity> findByPetOrderByRecordDateDesc(PetEntity pet);
    
    // 특정 반려동물의 특정 타입 건강 기록 조회
    List<HealthRecordEntity> findByPetAndRecordTypeOrderByRecordDateDesc(PetEntity pet, String recordType);
    
    // 특정 기간의 건강 기록 조회
    @Query("SELECT h FROM HealthRecordEntity h WHERE h.pet = :pet AND h.recordDate BETWEEN :startDate AND :endDate ORDER BY h.recordDate DESC")
    List<HealthRecordEntity> findByPetAndDateRange(@Param("pet") PetEntity pet, 
                                                    @Param("startDate") LocalDate startDate, 
                                                    @Param("endDate") LocalDate endDate);
    
    // 특정 반려동물의 예정된 기록 조회
    List<HealthRecordEntity> findByPetAndStatusOrderByRecordDateAsc(PetEntity pet, String status);
}