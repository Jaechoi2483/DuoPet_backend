package com.petlogue.duopetbackend.info.jpa.repository;

import com.petlogue.duopetbackend.info.jpa.entity.HospitalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<HospitalEntity, Long> {

    // 병원명으로 검색 (부분 일치) - NICKNAME 필드 사용
    List<HospitalEntity> findByHospitalNameContainingIgnoreCase(String hospitalName);

    // 주소로 검색 (부분 일치)
    List<HospitalEntity> findByAddressContaining(String address);

    // 병원명, 주소로 통합 검색
    @Query("SELECT h FROM HospitalEntity h WHERE " +
           "(LOWER(h.hospitalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<HospitalEntity> findByKeyword(@Param("keyword") String keyword);

    // role='vet'이고 활성 상태인 병원만 조회
    List<HospitalEntity> findByRoleAndStatus(String role, String status);
    
    // role='vet'인 모든 사용자 조회
    List<HospitalEntity> findByRole(String role);

    // 활성 상태인 병원만 조회 (role 조건 없음)
    List<HospitalEntity> findByStatus(String status);

    // 성별로 수의사 검색 (참고용)
    List<HospitalEntity> findByGender(String gender);

    // 지역별 검색 (주소에 특정 지역명 포함)
    @Query("SELECT h FROM HospitalEntity h WHERE h.address LIKE %:region%")
    List<HospitalEntity> findByRegion(@Param("region") String region);

    // 병원명과 주소로 복합 검색
    @Query("SELECT h FROM HospitalEntity h WHERE " +
           "(LOWER(h.hospitalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND h.status = 'active'")
    List<HospitalEntity> findActiveHospitalsByKeyword(@Param("keyword") String keyword);

    // 사용자 이름으로 검색 (수의사 이름)
    List<HospitalEntity> findByUserNameContainingIgnoreCase(String userName);

    // 이메일로 검색
    List<HospitalEntity> findByEmailContaining(String email);

    // 전화번호로 검색
    List<HospitalEntity> findByPhoneContaining(String phone);

    // 로그인 ID로 검색
    HospitalEntity findByLoginId(String loginId);

    // role='vet'이고 활성 상태이며 키워드 검색
    @Query("SELECT h FROM HospitalEntity h WHERE " +
           "h.role = 'vet' AND h.status = 'active' AND " +
           "(LOWER(h.hospitalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.userName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<HospitalEntity> findActiveVetsByKeywordSearch(@Param("keyword") String keyword);

    // 활성 상태이고 키워드 검색 (role 조건 없음)
    @Query("SELECT h FROM HospitalEntity h WHERE " +
           "h.status = 'active' AND " +
           "(LOWER(h.hospitalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.userName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<HospitalEntity> findActiveByKeywordSearch(@Param("keyword") String keyword);
}