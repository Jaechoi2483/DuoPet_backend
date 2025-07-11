package com.petlogue.duopetbackend.info.jpa.repository;

import com.petlogue.duopetbackend.info.jpa.entity.ShelterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("shelterInfoRepository")
public interface ShelterRepository extends JpaRepository<ShelterEntity, Long> {
    
    /**
     * 활성화된 보호소만 조회 (USERS 테이블과 조인)
     * USERS 테이블에서 role='shelter'이고 status='active'인 보호소들만 가져옴
     */
    @Query(value = """
        SELECT s.shelter_id, s.user_id, s.shelter_name, s.phone, s.email, 
               s.address, s.website, s.capacity, s.operating_hours,
               s.rename_filename, s.original_filename,
               u.user_name as manager_name, u.role, u.status
        FROM shelter s 
        INNER JOIN users u ON s.user_id = u.user_id 
        WHERE u.role = 'shelter' AND u.status = 'active'
        ORDER BY s.shelter_name
        """, nativeQuery = true)
    List<Object[]> findActiveShelters();
    
    /**
     * 키워드로 보호소 검색 (보호소명, 주소, 관리자명으로 검색)
     */
    @Query(value = """
        SELECT s.shelter_id, s.user_id, s.shelter_name, s.phone, s.email, 
               s.address, s.website, s.capacity, s.operating_hours,
               s.rename_filename, s.original_filename,
               u.user_name as manager_name, u.role, u.status
        FROM shelter s 
        INNER JOIN users u ON s.user_id = u.user_id 
        WHERE u.role = 'shelter' AND u.status = 'active'
        AND (LOWER(s.shelter_name) LIKE LOWER('%' || :keyword || '%') 
             OR LOWER(s.address) LIKE LOWER('%' || :keyword || '%')
             OR LOWER(u.user_name) LIKE LOWER('%' || :keyword || '%'))
        ORDER BY s.shelter_name
        """, nativeQuery = true)
    List<Object[]> findActiveSheltersByKeywordSearch(@Param("keyword") String keyword);
    
    /**
     * 페이징을 지원하는 활성화된 보호소 조회
     */
    @Query(value = """
        SELECT s.shelter_id, s.user_id, s.shelter_name, s.phone, s.email, 
               s.address, s.website, s.capacity, s.operating_hours,
               s.rename_filename, s.original_filename,
               u.user_name as manager_name, u.role, u.status
        FROM shelter s 
        INNER JOIN users u ON s.user_id = u.user_id 
        WHERE u.role = 'shelter' AND u.status = 'active'
        ORDER BY s.shelter_name
        """, 
        countQuery = """
        SELECT COUNT(*)
        FROM shelter s 
        INNER JOIN users u ON s.user_id = u.user_id 
        WHERE u.role = 'shelter' AND u.status = 'active'
        """,
        nativeQuery = true)
    Page<Object[]> findActiveSheltersWithPaging(Pageable pageable);
    
    /**
     * 특정 보호소 ID로 상세 정보 조회
     */
    @Query(value = """
        SELECT s.shelter_id, s.user_id, s.shelter_name, s.phone, s.email, 
               s.address, s.website, s.capacity, s.operating_hours,
               s.rename_filename, s.original_filename,
               u.user_name as manager_name, u.role, u.status
        FROM shelter s 
        INNER JOIN users u ON s.user_id = u.user_id 
        WHERE s.shelter_id = :shelterId AND u.role = 'shelter' AND u.status = 'active'
        """, nativeQuery = true)
    List<Object[]> findActiveShelterById(@Param("shelterId") Long shelterId);
}