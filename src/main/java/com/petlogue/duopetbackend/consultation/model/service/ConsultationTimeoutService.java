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

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationTimeoutService {

    private final ConsultationRoomRepository roomRepository;
    private final ConsultationNotificationService notificationService;

    // 5초마다 실행하여 타임아웃된 요청이 있는지 확인 (더 빠른 응답을 위해)
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkForTimeouts() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusSeconds(30);
        List<ConsultationRoom> timedOutRooms = roomRepository.findWaitingRoomsCreatedBefore(timeoutThreshold);

        for (ConsultationRoom room : timedOutRooms) {
            log.warn("상담 요청 타임아웃: Room ID {}, 생성 시간: {}", room.getRoomId(), room.getCreatedAt());
            
            // TIMED_OUT 상태로 변경
            room.setRoomStatus(ConsultationRoom.RoomStatus.TIMED_OUT.name());
            room.setEndedAt(LocalDateTime.now());
            room.setConsultationNotes("전문가가 30초 내에 응답하지 않아 자동으로 취소되었습니다.");
            
            ConsultationRoom savedRoom = roomRepository.save(room);

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