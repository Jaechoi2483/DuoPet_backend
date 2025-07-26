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

    // 10초마다 실행하여 타임아웃된 요청이 있는지 확인
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkForTimeouts() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusSeconds(30);
        List<ConsultationRoom> timedOutRooms = roomRepository.findWaitingRoomsCreatedBefore(timeoutThreshold);

        for (ConsultationRoom room : timedOutRooms) {
            log.warn("상담 요청 타임아웃: Room ID {}, 생성 시간: {}", room.getRoomId(), room.getCreatedAt());
            
            room.setRoomStatus("CANCELLED");
            room.setEndedAt(LocalDateTime.now());
            room.setConsultationNotes("전문가가 30초 내에 응답하지 않아 자동으로 취소되었습니다.");
            
            roomRepository.save(room);

            notificationService.sendStatusChangeNotification(room, "TIMED_OUT");
        }
    }
}