package com.petlogue.duopetbackend.user.jpa.repository;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VetRepository extends JpaRepository<VetEntity, Long> {

    // [중복 방지] 해당 유저가 이미 전문가인지 확인
    boolean existsByUser(UserEntity user);

    // [조회] UserEntity 기반으로 전문가 정보 가져오기
    Optional<VetEntity> findByUser(UserEntity user);

    // [조회] userId 기반으로 전문가 정보 가져오기
    Optional<VetEntity> findByUser_UserId(Long userId);
}
