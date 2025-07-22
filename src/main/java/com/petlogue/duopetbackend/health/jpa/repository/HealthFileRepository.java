package com.petlogue.duopetbackend.health.jpa.repository;

import com.petlogue.duopetbackend.health.jpa.entity.HealthFileEntity;
import com.petlogue.duopetbackend.health.jpa.entity.HealthRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthFileRepository extends JpaRepository<HealthFileEntity, Long> {
    
    // 특정 건강 기록의 파일 목록 조회
    List<HealthFileEntity> findByHealthRecord(HealthRecordEntity healthRecord);
    
    // 특정 건강 기록의 파일 삭제
    void deleteByHealthRecord(HealthRecordEntity healthRecord);
}