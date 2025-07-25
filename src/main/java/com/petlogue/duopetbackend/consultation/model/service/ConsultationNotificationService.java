package com.petlogue.duopetbackend.consultation.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.model.dto.ConsultationNotificationDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 수의사에게 새로운 상담 요청 알림 전송
     */
    public void sendNewConsultationNotification(ConsultationRoom room) {
        try {
            ConsultationNotificationDto notification = ConsultationNotificationDto.builder()
                    .type("NEW_CONSULTATION")
                    .roomId(room.getRoomId())
                    .roomUuid(room.getRoomUuid())
                    .userId(room.getUser().getUserId())
                    .userName(room.getUser().getUserName())
                    .petName(room.getPet() != null ? room.getPet().getPetName() : null)
                    .chiefComplaint(room.getChiefComplaint())
                    .consultationType(room.getConsultationType())
                    .timestamp(room.getCreatedAt())
                    .build();
            
            // 수의사의 user destination으로 전송 - loginId 사용
            String destination = "/queue/consultations";
            String vetLoginId = room.getVet().getUser().getLoginId();
            messagingTemplate.convertAndSendToUser(
                    vetLoginId, // 수의사의 loginId를 username으로 사용
                    destination,
                    notification
            );
            
            log.info("Sent consultation notification to vet {} (loginId: {}) for room {}", 
                    room.getVet().getVetId(), vetLoginId, room.getRoomUuid());
            
        } catch (Exception e) {
            log.error("Failed to send consultation notification", e);
        }
    }
    
    /**
     * 상담 상태 변경 알림 전송
     */
    public void sendStatusChangeNotification(ConsultationRoom room, String status) {
        try {
            ConsultationNotificationDto notification = ConsultationNotificationDto.builder()
                    .type("STATUS_CHANGE")
                    .roomId(room.getRoomId())
                    .roomUuid(room.getRoomUuid())
                    .status(status)
                    .timestamp(room.getUpdatedAt())
                    .build();
            
            // 사용자와 수의사 모두에게 전송
            messagingTemplate.convertAndSend(
                    "/topic/consultation/" + room.getRoomUuid() + "/status",
                    notification
            );
            
            log.info("Sent status change notification for room {} to status {}", 
                    room.getRoomUuid(), status);
            
        } catch (Exception e) {
            log.error("Failed to send status change notification", e);
        }
    }
}