package com.petlogue.duopetbackend.user.jpa.repository;

import com.petlogue.duopetbackend.user.jpa.entity.ShelterEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("shelterUserRepository")
public interface ShelterRepository extends JpaRepository<ShelterEntity, Long> {

    // 이미 등록된 보호소인지 확인
    boolean existsByUser(UserEntity user);

    // UserEntity로 보호소 정보 조회
    Optional<ShelterEntity> findByUser(UserEntity user);

    // userId 기준으로 보호소 정보 조회
    Optional<ShelterEntity> findByUser_UserId(Long userId);
}
