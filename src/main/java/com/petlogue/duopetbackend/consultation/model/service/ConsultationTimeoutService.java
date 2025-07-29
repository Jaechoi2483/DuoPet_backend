// /consultation/model/service/ConsultationTimeoutService.java
package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationTimeoutService {

    private final ConsultationRoomRepository roomRepository;
    private final ConsultationNotificationService notificationService;
    
    // 메모리 기반 시간 추적 - DB 시간에 의존하지 않음
    private final Map<Long, LocalDateTime> roomCreationTimes = new ConcurrentHashMap<>();
    
    /**
     * 상담방 생성 시 호출되어 메모리에 생성 시간 저장
     */
    public void registerConsultationRoom(Long roomId) {
        LocalDateTime now = LocalDateTime.now();
        roomCreationTimes.put(roomId, now);
        log.info("상담방 {} 생성 시간 등록: {}", roomId, now);
    }
    
    /**
     * 상담방 종료 시 메모리에서 제거
     */
    public void unregisterConsultationRoom(Long roomId) {
        roomCreationTimes.remove(roomId);
        log.info("상담방 {} 메모리에서 제거", roomId);
    }

    // 5초마다 실행하여 타임아웃된 요청이 있는지 확인 (더 빠른 응답을 위해)
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkForTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        // DB 시간대 문제 해결: DB와 JVM의 시간 차이가 약 30-40초 정도 발생하는 것으로 추정
        // 200초로 설정하여 실제로 30초가 지난 후에만 타임아웃 처리되도록 함
        LocalDateTime timeoutThreshold = now.minusSeconds(200);
        
        log.warn("타임아웃 체크 시작 - JVM 현재 시간: {}, DB 조회 기준 시간: {} (200초 전)", 
                now, timeoutThreshold);
        
        List<ConsultationRoom> timedOutRooms = roomRepository.findWaitingRoomsCreatedBefore(timeoutThreshold);

        log.warn("타임아웃 대상 상담방 수: {}", timedOutRooms.size());
        
        for (ConsultationRoom room : timedOutRooms) {
            LocalDateTime roomCreatedAt = room.getCreatedAt();
            long secondsSinceCreation = java.time.Duration.between(roomCreatedAt, now).getSeconds();
            
            log.error("!!! 중요 !!! 상담 타임아웃 체크 - Room ID: {}", room.getRoomId());
            log.error("DB에 저장된 생성 시간: {}", roomCreatedAt);
            log.error("JVM 현재 시간: {}", now);
            log.error("계산된 경과 시간: {}초", secondsSinceCreation);
            log.error("타임아웃 기준: 30초");
            
            // 메모리 기반 시간 체크
            LocalDateTime memoryCreatedAt = roomCreationTimes.get(room.getRoomId());
            if (memoryCreatedAt != null) {
                long memoryBasedSeconds = java.time.Duration.between(memoryCreatedAt, now).getSeconds();
                log.error("메모리 기반 경과 시간: {}초", memoryBasedSeconds);
                
                // 메모리 기반으로 30초 미만이면 건너뛰기
                if (memoryBasedSeconds < 30) {
                    log.error("메모리 기반 체크: 30초 미만 ({})초이므로 타임아웃 처리 건너뛰기", memoryBasedSeconds);
                    continue;
                }
            }
            
            // 실제로 30초가 지났는지 다시 한번 확인 (DB 기반)
            if (secondsSinceCreation < 30) {
                log.error("DB 기반 체크: 30초 미만인데 타임아웃 처리됨! Room ID: {}, 경과 시간: {}초", 
                        room.getRoomId(), secondsSinceCreation);
                // 메모리 기반 체크가 없거나 실패한 경우에만 여기서 처리
                if (memoryCreatedAt == null) {
                    continue; // 타임아웃 처리 건너뛰기
                }
            }
            
            // TIMED_OUT 상태로 변경
            room.setRoomStatus(ConsultationRoom.RoomStatus.TIMED_OUT.name());
            room.setEndedAt(LocalDateTime.now());
            room.setConsultationNotes("전문가가 30초 내에 응답하지 않아 자동으로 취소되었습니다.");
            
            ConsultationRoom savedRoom = roomRepository.save(room);
            
            // 메모리에서 제거
            unregisterConsultationRoom(room.getRoomId());

            // 타임아웃 알림 전송 - 여러 번 시도
            for (int i = 0; i < 3; i++) {
                try {
                    notificationService.sendStatusChangeNotification(savedRoom, "TIMED_OUT");
                    log.info("타임아웃 알림 전송 성공 (시도 {}번) - roomUuid: {}", i + 1, savedRoom.getRoomUuid());
                    break; // 성공하면 중단
                } catch (Exception e) {
                    log.error("타임아웃 알림 전송 실패 (시도 {}번)", i + 1, e);
                    if (i < 2) {
                        try {
                            Thread.sleep(1000); // 1초 대기 후 재시도
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
    }
}