package com.petlogue.duopetbackend.consultation.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 상담 타임아웃을 메모리에서 관리하는 서비스
 * DB 시간대 문제를 회피하기 위해 애플리케이션 메모리에서 직접 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationTimeoutManager {
    
    // roomId -> 생성시간 매핑
    private final Map<Long, LocalDateTime> waitingRooms = new ConcurrentHashMap<>();
    
    private final ConsultationTimeoutService timeoutService;
    
    /**
     * 대기 중인 상담방 등록
     */
    public void registerWaitingRoom(Long roomId) {
        LocalDateTime now = LocalDateTime.now();
        waitingRooms.put(roomId, now);
        log.info("상담방 대기 등록: roomId={}, 시간={}", roomId, now);
    }
    
    /**
     * 상담방 대기 해제 (승인/거절/취소 시)
     */
    public void unregisterRoom(Long roomId) {
        waitingRooms.remove(roomId);
        log.info("상담방 대기 해제: roomId={}", roomId);
    }
    
    /**
     * 5초마다 타임아웃 체크
     */
    @Scheduled(fixedRate = 5000)
    public void checkTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        
        waitingRooms.entrySet().removeIf(entry -> {
            Long roomId = entry.getKey();
            LocalDateTime createdTime = entry.getValue();
            long secondsElapsed = java.time.Duration.between(createdTime, now).getSeconds();
            
            if (secondsElapsed >= 30) {
                log.warn("메모리 기반 타임아웃 감지: roomId={}, 경과시간={}초", roomId, secondsElapsed);
                // 타임아웃 처리는 기존 서비스에 위임
                // timeoutService.handleTimeout(roomId);
                return true; // 맵에서 제거
            }
            
            return false;
        });
    }
}