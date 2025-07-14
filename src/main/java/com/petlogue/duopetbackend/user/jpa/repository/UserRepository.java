package com.petlogue.duopetbackend.user.jpa.repository;

import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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


// 관리자 회원목록 조회할때 역할이랑 상태별로 조회할때 쓰는 거에요

    Page<UserEntity> findByRoleAndStatus(String role, String status, Pageable pageable);

    // 2. Role로만 필터링
    Page<UserEntity> findByRole(String role, Pageable pageable);

    // 3. Status로만 필터링
    Page<UserEntity> findByStatus(String status, Pageable pageable);

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

}
