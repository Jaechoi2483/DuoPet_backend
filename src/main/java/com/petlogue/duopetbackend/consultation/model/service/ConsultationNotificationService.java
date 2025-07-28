package com.petlogue.duopetbackend.consultation.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.model.dto.ConsultationNotificationDto;
import java.time.LocalDateTime;

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
            
            // 수의사가 지정된 경우 해당 수의사에게만 전송
            if (room.getVet() != null) {
                // 특정 수의사에게만 전송
                String destination = "/queue/consultations";
                String vetLoginId = room.getVet().getUser().getLoginId();
                messagingTemplate.convertAndSendToUser(
                        vetLoginId, // 수의사의 loginId를 username으로 사용
                        destination,
                        notification
                );
                
                log.info("Sent consultation notification to vet {} (loginId: {}) for room {}", 
                        room.getVet().getVetId(), vetLoginId, room.getRoomUuid());
            } else {
                // 수의사가 지정되지 않은 경우 모든 수의사에게 브로드캐스트
                String destination = "/topic/vets/consultations";
                messagingTemplate.convertAndSend(destination, notification);
                
                log.info("Broadcast new consultation notification to all vets for room {}", 
                        room.getRoomUuid());
            }
            
        } catch (Exception e) {
            log.error("Failed to send consultation notification", e);
        }
    }
    
    /**
     * 상담 상태 변경 알림 전송
     */
    public void sendStatusChangeNotification(ConsultationRoom room, String status) {
        try {
            log.info("상태 변경 알림 전송 시작 - roomUuid: {}, status: {}", room.getRoomUuid(), status);
            
            ConsultationNotificationDto notification = ConsultationNotificationDto.builder()
                    .type("STATUS_CHANGE")
                    .roomId(room.getRoomId())
                    .roomUuid(room.getRoomUuid())
                    .status(status)
                    .timestamp(room.getUpdatedAt() != null ? room.getUpdatedAt() : LocalDateTime.now())
                    .build();
            
            String destination = "/topic/consultation/" + room.getRoomUuid() + "/status";
            log.info("WebSocket 전송 대상: {}", destination);
            
            // 사용자와 수의사 모두에게 전송
            messagingTemplate.convertAndSend(destination, notification);
            
            log.info("상태 변경 알림 전송 완료 - roomUuid: {}, status: {}, destination: {}", 
                    room.getRoomUuid(), status, destination);
            
        } catch (Exception e) {
            log.error("상태 변경 알림 전송 실패 - roomUuid: {}, status: {}", room.getRoomUuid(), status, e);
            throw e; // 예외를 다시 던져서 호출자가 알 수 있도록 함
        }
    }
}