package com.petlogue.duopetbackend.user.jpa.repository;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // [로그인, 인증용] loginId로 회원 조회
    Optional<UserEntity> findByLoginId(String loginId);

    // [회원가입 1단계] loginId 중복 여부 확인
    boolean existsByLoginId(String loginId);

    // [회원가입 등] 이메일 중복 여부 확인
    boolean existsByUserEmail(String userEmail);
}
