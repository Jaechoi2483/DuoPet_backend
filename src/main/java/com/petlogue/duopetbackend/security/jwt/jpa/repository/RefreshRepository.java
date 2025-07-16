package com.petlogue.duopetbackend.security.jwt.jpa.repository;

import com.petlogue.duopetbackend.security.jwt.jpa.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

    // userId로 refreshToken 조회
    Optional<RefreshToken> findByUserId(Long userId);

    // userId로 refreshToken 조회
    @Query("SELECT r.refreshToken FROM RefreshToken r WHERE r.userId = :userId")
    String findTokenByUserId(@Param("userId") Long userId);

    // userId와 refreshToken 값으로 tokenId 조회
    @Query("SELECT r.tokenId FROM RefreshToken r WHERE r.userId = :userId AND r.refreshToken = :token")
    Long findIdByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);

    // tokenId 기준으로 토큰 갱신
    @Modifying
    @Query("UPDATE RefreshToken r SET r.refreshToken = :token WHERE r.tokenId = :id")
    int updateTokenById(@Param("id") Long id, @Param("token") String token);

    // RefreshToken 전체 조회
    Optional<RefreshToken> findByRefreshToken(String token);

    // RefreshToken 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
