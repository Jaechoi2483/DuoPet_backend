package com.petlogue.duopetbackend.security.jwt.model.service;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.security.jwt.jpa.entity.RefreshToken;
import com.petlogue.duopetbackend.security.jwt.jpa.repository.RefreshRepository ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class RefreshService {

    private final RefreshRepository refreshTokenRepository;
    private final JWTUtil jwtUtil; // JWTUtil 주입

    // 기본 저장
    public void saveRefresh(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void saveOrUpdate(Long userId, String tokenValue, String ipAddress, String deviceInfo) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);

        // 만료 시간 계산
        Date expiresAt = new Date(System.currentTimeMillis() + jwtUtil.getRefreshExpiration());

        RefreshToken tokenToSave;
        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트
            tokenToSave = existingToken.get();
            tokenToSave.updateToken(tokenValue, ipAddress, deviceInfo, expiresAt);
        } else {
            // 새 토큰 생성
            tokenToSave = RefreshToken.builder()
                    .userId(userId)
                    .refreshToken(tokenValue)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .expiresAt(expiresAt) // 만료 시간 설정
                    .tokenStatus("ACTIVE")
                    .build();
        }
        refreshTokenRepository.save(tokenToSave);
    }

    // 소셜 저장
    public void saveToken(Long userId, String refreshTokenValue) {
        Date expiresAt = new Date(System.currentTimeMillis() + jwtUtil.getRefreshExpiration());
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .refreshToken(refreshTokenValue)
                .expiresAt(expiresAt)
                .build();

        saveRefresh(refreshToken);
    }

    // 삭제
    public void deleteRefresh(Long tokenId) {
        refreshTokenRepository.deleteById(tokenId);
    }

    // userId 기준 전체 삭제 (로그아웃 용)
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    // 토큰값 수정
    public int updateRefreshToken(Long tokenId, String tokenValue) {
        return refreshTokenRepository.updateTokenById(tokenId, tokenValue);
    }

    // userId + 토큰으로 ID 조회
    public Long findTokenId(Long userId, String refreshToken) {
        return refreshTokenRepository.findIdByUserIdAndToken(userId, refreshToken);
    }

    // userId로 현재 refreshToken 조회
    public String findTokenByUserId(Long userId) {
        return refreshTokenRepository.findTokenByUserId(userId);
    }

    // 토큰 문자열로 전체 객체 조회
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token);
    }

    public void deleteRefreshByUserId(Long userId) {
        Optional<RefreshToken> entity = refreshTokenRepository.findByUserId(userId);
        entity.ifPresent(refreshTokenRepository::delete);
    }

    public void saveRefreshToken(Long userId, String newRefreshToken) {
        Date expiresAt = new Date(System.currentTimeMillis() + jwtUtil.getRefreshExpiration());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .refreshToken(newRefreshToken)
                .expiresAt(expiresAt)
                .tokenStatus("ACTIVE")
                .build();

        refreshTokenRepository.save(refreshToken);
    }
}
