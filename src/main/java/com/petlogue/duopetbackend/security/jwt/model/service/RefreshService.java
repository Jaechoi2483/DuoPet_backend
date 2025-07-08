package com.petlogue.duopetbackend.security.jwt.model.service;

import com.petlogue.duopetbackend.security.jwt.jpa.entity.RefreshToken;
import com.petlogue.duopetbackend.security.jwt.jpa.repository.RefreshRepository ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class RefreshService {

    private final RefreshRepository refreshTokenRepository;

    // 저장
    public void saveRefresh(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
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
}
