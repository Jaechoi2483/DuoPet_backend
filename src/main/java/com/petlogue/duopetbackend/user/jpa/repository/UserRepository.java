package com.petlogue.duopetbackend.user.jpa.repository;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 로그인 시 사용: loginId로 회원 조회
    Optional<UserEntity> findByLoginId(String loginId);

    // 이메일 중복 체크 등 추가 확장 가능
    boolean existsByUserEmail(String userEmail);
}
