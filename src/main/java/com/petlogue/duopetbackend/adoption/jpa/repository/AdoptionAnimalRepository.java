package com.petlogue.duopetbackend.adoption.jpa.repository;

import com.petlogue.duopetbackend.adoption.jpa.entity.AdoptionAnimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionAnimalRepository extends JpaRepository<AdoptionAnimal, Long> {
    
    // 유기번호로 동물 찾기 (중복 체크용)
    Optional<AdoptionAnimal> findByDesertionNo(String desertionNo);
    
    // 유기번호 존재 여부 확인 (중복 체크용)
    boolean existsByDesertionNo(String desertionNo);
    
    // 보호 중인 동물만 조회
    @Query("SELECT a FROM AdoptionAnimal a WHERE a.processState IN ('protect', '보호중') AND a.status = 'AVAILABLE'")
    Page<AdoptionAnimal> findAvailableAnimals(Pageable pageable);
    
    // 지역별 보호 동물 조회
    @Query("SELECT a FROM AdoptionAnimal a LEFT JOIN a.shelter s " +
           "WHERE a.processState IN ('protect', '보호중') " +
           "AND (s.address LIKE %:region% OR a.apiShelterAddr LIKE %:region%)")
    Page<AdoptionAnimal> findByRegion(@Param("region") String region, Pageable pageable);
    
    // 동물 종류별 조회
    @Query("SELECT a FROM AdoptionAnimal a " +
           "WHERE a.processState IN ('protect', '보호중') " +
           "AND a.animalType = :type")
    Page<AdoptionAnimal> findByAnimalType(@Param("type") String type, Pageable pageable);
    
    // 메인 화면용 랜덤 동물 조회
    @Query(value = "SELECT * FROM (" +
           "SELECT * FROM SHELTER_ANIMALS " +
           "WHERE process_state IN ('protect', '보호중') AND status = 'AVAILABLE' " +
           "ORDER BY DBMS_RANDOM.VALUE) " +
           "WHERE ROWNUM <= :limit", nativeQuery = true)
    List<AdoptionAnimal> findRandomAnimals(@Param("limit") int limit);
    
    // 보호소별 동물 조회
    @Query("SELECT a FROM AdoptionAnimal a WHERE a.shelter.shelterId = :shelterId")
    List<AdoptionAnimal> findByShelterId(@Param("shelterId") Long shelterId);
    
    // 복합 검색 (지역, 동물종류, 성별, 중성화여부)
    @Query("SELECT a FROM AdoptionAnimal a LEFT JOIN a.shelter s " +
           "WHERE a.processState IN ('protect', '보호중') " +
           "AND a.status = 'AVAILABLE' " +
           "AND (:region IS NULL OR s.address LIKE %:region% OR a.apiShelterAddr LIKE %:region%) " +
           "AND (:type IS NULL OR a.animalType = :type) " +
           "AND (:gender IS NULL OR a.gender = :gender) " +
           "AND (:neutered IS NULL OR a.neutered = :neutered)")
    Page<AdoptionAnimal> searchAnimals(@Param("region") String region,
                                      @Param("type") String type,
                                      @Param("gender") String gender,
                                      @Param("neutered") String neutered,
                                      Pageable pageable);
    
    // 최근 저장된 동물 조회
    List<AdoptionAnimal> findTop5ByOrderByCreatedAtDesc();
}