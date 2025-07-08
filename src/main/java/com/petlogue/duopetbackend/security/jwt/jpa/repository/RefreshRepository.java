package com.petlogue.duopetbackend.security.jwt.jpa.repository;

import com.petlogue.duopetbackend.security.jwt.jpa.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

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

    // userId 기준 전체 삭제 (로그아웃 시 유용)
    void deleteByUserId(Long userId);
}
