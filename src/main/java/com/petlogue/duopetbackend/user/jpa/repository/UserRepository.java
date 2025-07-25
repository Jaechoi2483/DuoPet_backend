package com.petlogue.duopetbackend.user.jpa.repository;


import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.model.dto.UserDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // [로그인, 인증용] loginId로 회원 조회
    Optional<UserEntity> findByLoginId(String loginId);

    // [회원가입 1단계] loginId 중복 여부 확인
    boolean existsByLoginId(String loginId);

    // [회원가입 등] 이메일 중복 여부 확인
    boolean existsByUserEmail(String userEmail);

    // [회원가입 2단계] 닉네임 중복 여부 확인
    boolean existsByNickname(String nickname);

    // userNo로 사용자 조회
    UserEntity findByUserId(Long userId);

    // 소셜 기기 종류와 소셜아이디로 사용자 조회
    Optional<UserEntity> findByProviderAndProviderId(String provider, String providerId);

    // loginId와 phone이 일치하는 사용자가 있는지 확인
    @Query("SELECT u FROM UserEntity u WHERE u.userName = :userName AND REPLACE(u.phone, '-', '') = :phone")
    Optional<UserEntity> findByUserNameAndPhoneWithoutHyphen(@Param("userName") String userName, @Param("phone") String phone);

    // 유저 아이디와 일치하는 전화번
    @Query("SELECT u FROM UserEntity u WHERE u.loginId = :loginId AND REPLACE(u.phone, '-', '') = :phone")
    Optional<UserEntity> findByLoginIdAndPhoneWithoutHyphen(@Param("loginId") String loginId, @Param("phone") String phone);


// 관리자 회원목록 조회할때 역할이랑 상태별로 조회할때 쓰는 거에요

    Page<UserEntity> findByRoleAndStatus(String role, String status, Pageable pageable);

    // 2. Role로만 필터링
    Page<UserEntity> findByRole(String role, Pageable pageable);

    // 3. Status로만 필터링
    Page<UserEntity> findByStatus(String status, Pageable pageable);




    @Query("SELECT NEW com.petlogue.duopetbackend.user.model.dto.UserDetailDto(u, v, s) " +
            "FROM UserEntity u " +
            "LEFT JOIN VetEntity v ON v.user.userId = u.userId " +
            "LEFT JOIN UserShelterEntity s ON s.user.userId = u.userId " +
            "WHERE u.userId = :userId")
    Optional<UserDetailDto> findDetailWithProfiles(@Param("userId") Long userId);

    // 관리자 페이지 통계 표시할떄  쓰는 거에요
    @Query("SELECT new com.petlogue.duopetbackend.admin.model.dto.StatItemDto(u.gender, COUNT(u)) " +
            "FROM UserEntity u GROUP BY u.gender")
    List<StatItemDto> findGenderStat();

    @Query(value = "SELECT " +
            "    CAST(pet_counts.pet_count AS VARCHAR2(255)) as item, " +
            "    CAST(COUNT(pet_counts.user_id) AS NUMBER(19)) as count " + // ✨ NUMBER -> NUMBER(19)로 수정
            "FROM " +
            "    (SELECT " +
            "        u.user_id, " +
            "        COUNT(p.pet_id) as pet_count " +
            "    FROM " +
            "        users u " +
            "    LEFT JOIN " +
            "        pet p ON u.user_id = p.user_id " +
            "    GROUP BY " +
            "        u.user_id) pet_counts " +
            "GROUP BY " +
            "    pet_counts.pet_count " +
            "ORDER BY " +
            "    pet_counts.pet_count ASC", nativeQuery = true)
    List<StatItemDto> findPetCountStat();

    long countByRole(String role);

    Long userId(Long userId);


    // 신고관리용

    

    @Query("SELECT u FROM UserEntity u WHERE UPPER(u.status) = 'SUSPENDED' AND u.suspendedUntil IS NOT NULL AND u.suspendedUntil <= :now")
    List<UserEntity> findExpiredSuspensions(@Param("now") LocalDateTime now);
}
