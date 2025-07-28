package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.model.dto.ConsultationRoomDto;
import com.petlogue.duopetbackend.consultation.model.dto.CreateConsultationDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom.RoomStatus;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetProfile;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetSchedule;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository;
import com.petlogue.duopetbackend.consultation.jpa.repository.VetProfileRepository;
import com.petlogue.duopetbackend.consultation.jpa.repository.VetScheduleRepository;
import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.jpa.repository.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationRoomService {
    
    private final ConsultationRoomRepository consultationRoomRepository;
    private final UserRepository userRepository;
    private final VetRepository vetRepository;
    private final PetRepository petRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final VetProfileRepository vetProfileRepository;
    private final VetScheduleRepository vetScheduleRepository;
    private final ConsultationNotificationService notificationService;
    
    /**
     * 상담방 생성
     */
    @Transactional
    public ConsultationRoom createConsultationRoom(CreateConsultationDto dto) {
        // Validate user
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Validate vet
        VetEntity vet = vetRepository.findById(dto.getVetId())
                .orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        
        // Validate pet if provided
        PetEntity pet = null;
        if (dto.getPetId() != null) {
            pet = petRepository.findById(dto.getPetId())
                    .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
            
            // Verify pet belongs to user
            if (!pet.getUser().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Pet does not belong to user");
            }
        }
        
        // Get vet profile for consultation fee
        VetProfile vetProfile = vetProfileRepository.findByVet_VetId(vet.getVetId())
                .orElseThrow(() -> new IllegalArgumentException("Vet profile not found"));
        
        // Handle schedule if provided
        VetSchedule schedule = null;
        if (dto.getScheduleId() != null) {
            schedule = vetScheduleRepository.findById(dto.getScheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
            
            // Verify schedule belongs to vet and is available
            if (!schedule.getVet().getVetId().equals(vet.getVetId()) || 
                !schedule.isAvailable()) {
                throw new IllegalArgumentException("Schedule is not available");
            }
        }
        
        // Create consultation room
        ConsultationRoom room = ConsultationRoom.builder()
                .roomUuid(UUID.randomUUID().toString())
                .user(user)
                .vet(vet)
                .pet(pet)
                .schedule(schedule)
                .consultationType(dto.getConsultationType() != null ? 
                        dto.getConsultationType() : "CHAT")
                .scheduledDatetime(dto.getScheduledDatetime())
                .consultationFee(vetProfile.getConsultationFee())
                .chiefComplaint(dto.getChiefComplaint())
                .roomStatus(schedule == null ? "WAITING" : "CREATED") // 즉시 상담은 WAITING, 예약 상담은 CREATED
                .build();
        
        // Update schedule status if scheduled consultation
        if (schedule != null) {
            schedule.incrementBooking();
            vetScheduleRepository.save(schedule);
        }
        
        ConsultationRoom savedRoom = consultationRoomRepository.save(room);
        
        // 상담방이 생성되면 수의사에게 알림 전송
        // 예약 상담이든 즉시 상담이든 상관없이 알림 전송
        notificationService.sendNewConsultationNotification(savedRoom);
        
        log.info("상담방 생성 완료: roomId={}, vetId={}, userId={}", 
                savedRoom.getRoomId(), savedRoom.getVet().getVetId(), savedRoom.getUser().getUserId());
        
        return savedRoom;
    }
    
    /**
     * 상담방 조회 (UUID)
     */
    @Transactional(readOnly = true)
    public ConsultationRoom getConsultationRoomByUuid(String roomUuid) {
        return consultationRoomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
    }
    
    /**
     * 사용자의 상담 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ConsultationRoom> getUserConsultations(Long userId, Pageable pageable) {
        return consultationRoomRepository.findByUserId(userId, pageable);
    }
    
    /**
     * 수의사의 상담 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ConsultationRoom> getVetConsultations(Long vetId, Pageable pageable) {
        return consultationRoomRepository.findByVetId(vetId, pageable);
    }
    
    /**
     * 오늘의 예약 상담 조회 (수의사용)
     */
    @Transactional(readOnly = true)
    public List<ConsultationRoom> getTodayScheduledConsultations(Long vetId) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        return consultationRoomRepository.findTodayScheduledConsultations(vetId, todayStart, todayEnd);
    }
    
    /**
     * 상담 시작
     */
    @Transactional
    public void startConsultation(Long roomId) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        if (!"CREATED".equals(room.getRoomStatus()) && !"WAITING".equals(room.getRoomStatus())) {
            throw new IllegalStateException("Cannot start consultation in current status: " + room.getRoomStatus());
        }
        
        room.startConsultation();
        consultationRoomRepository.save(room);
        
        log.info("Consultation started: room={}", roomId);
    }
    
    /**
     * ID로 상담 조회
     */
    public ConsultationRoom getConsultationById(Long roomId) {
        return consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
    }
    
    /**
     * 상담 종료
     */
    @Transactional
    public void endConsultation(Long roomId) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        if (!"IN_PROGRESS".equals(room.getRoomStatus())) {
            throw new IllegalStateException("Cannot end consultation in current status: " + room.getRoomStatus());
        }
        
        room.endConsultation();
        consultationRoomRepository.save(room);
        
        log.info("Consultation ended: room={}, duration={} minutes", 
                roomId, room.getDurationMinutes());
        
        // WebSocket으로 상담 종료 알림
        try {
            messagingTemplate.convertAndSend(
                "/topic/consultation/" + room.getRoomUuid() + "/status",
                Map.of(
                    "type", "CONSULTATION_ENDED",
                    "roomUuid", room.getRoomUuid(),
                    "status", "COMPLETED",
                    "message", "상담이 종료되었습니다."
                )
            );
            log.info("상담 종료 알림 전송 완료 - roomUuid: {}", room.getRoomUuid());
        } catch (Exception e) {
            log.error("상담 종료 알림 전송 실패", e);
        }
    }
    
    /**
     * 상담 취소
     */
    @Transactional
    public void cancelConsultation(Long roomId, String reason) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        if ("COMPLETED".equals(room.getRoomStatus()) || "CANCELLED".equals(room.getRoomStatus())) {
            throw new IllegalStateException("Cannot cancel consultation in current status: " + room.getRoomStatus());
        }
        
        room.cancelConsultation();
        room.setConsultationNotes("Cancellation reason: " + reason);
        
        // Release schedule if exists
        if (room.getSchedule() != null) {
            VetSchedule schedule = room.getSchedule();
            schedule.decrementBooking();
            vetScheduleRepository.save(schedule);
        }
        
        consultationRoomRepository.save(room);
        
        log.info("Consultation cancelled: room={}, reason={}", roomId, reason);
    }
    
    /**
     * 진행 중인 상담방 조회
     */
    @Transactional(readOnly = true)
    public List<ConsultationRoom> getActiveRooms(Long userId) {
        return consultationRoomRepository.findActiveRoomsByUserId(userId);
    }
    
    /**
     * 상담 노트 업데이트
     */
    @Transactional
    public void updateConsultationNotes(Long roomId, String notes) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        room.setConsultationNotes(notes);
        consultationRoomRepository.save(room);
    }
    
    /**
     * 처방전 업데이트
     */
    @Transactional
    public void updatePrescription(Long roomId, String prescription) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        room.setPrescription(prescription);
        consultationRoomRepository.save(room);
    }
    
    /**
     * 결제 완료 처리
     */
    @Transactional
    public void markAsPaid(Long roomId, String paymentMethod) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        room.markAsPaid(paymentMethod);
        consultationRoomRepository.save(room);
        
        log.info("Payment completed: room={}, method={}", roomId, paymentMethod);
    }
    
    /**
     * 상담 승인
     */
    @Transactional
    public ConsultationRoom approveConsultation(Long roomId) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("상담방을 찾을 수 없습니다."));
        
        // 상태 검증 - TIMED_OUT, REJECTED, CANCELLED 상태도 체크
        if (!"CREATED".equals(room.getRoomStatus()) && !"WAITING".equals(room.getRoomStatus())) {
            if ("TIMED_OUT".equals(room.getRoomStatus())) {
                throw new IllegalStateException("시간 초과된 상담입니다.");
            } else if ("REJECTED".equals(room.getRoomStatus())) {
                throw new IllegalStateException("이미 거절된 상담입니다.");
            } else if ("CANCELLED".equals(room.getRoomStatus())) {
                throw new IllegalStateException("취소된 상담입니다.");
            }
            throw new IllegalStateException("대기 중인 상담만 승인할 수 있습니다. 현재 상태: " + room.getRoomStatus());
        }
        
        // 상태 변경 - APPROVED 상태가 없는 경우 IN_PROGRESS로 변경
        room.setRoomStatus("IN_PROGRESS");
        room.setStartedAt(LocalDateTime.now());
        
        // 저장
        ConsultationRoom savedRoom = consultationRoomRepository.save(room);
        
        // 승인 알림 전송
        try {
            notificationService.sendStatusChangeNotification(savedRoom, "APPROVED");
            log.info("승인 알림 전송 완료 - roomUuid: {}", savedRoom.getRoomUuid());
        } catch (Exception e) {
            log.error("승인 알림 전송 실패", e);
        }
        
        log.info("Consultation room {} approved by vet {}", roomId, room.getVet().getVetId());
        
        return savedRoom;
    }

    /**
     * 상담 거절
     */
    @Transactional
    public ConsultationRoom rejectConsultation(Long roomId) {
        log.info("상담 거절 처리 시작 - roomId: {}", roomId);
        
        // 1. 상담방 조회
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("상담방을 찾을 수 없습니다."));
        
        log.info("상담방 조회 성공 - roomId: {}, currentStatus: {}", roomId, room.getRoomStatus());
        
        // 2. 상태 검증
        String currentStatus = room.getRoomStatus();
        if (!"CREATED".equals(currentStatus) && !"WAITING".equals(currentStatus)) {
            String errorMsg = String.format("대기 중인 상담만 거절할 수 있습니다. 현재 상태: %s", currentStatus);
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        
        // 3. 상태 변경
        try {
            room.setRoomStatus(RoomStatus.REJECTED.name());  // Enum 사용
            room.setEndedAt(LocalDateTime.now());
            room.setConsultationNotes("상담이 전문가에 의해 거절되었습니다.");
            log.info("상담방 상태 변경 완료 - roomId: {}, newStatus: REJECTED", roomId);
        } catch (Exception e) {
            log.error("상담방 상태 변경 중 오류", e);
            throw new RuntimeException("상담방 상태 변경 실패", e);
        }
        
        // 4. DB 저장
        ConsultationRoom savedRoom;
        try {
            savedRoom = consultationRoomRepository.save(room);
            log.info("상담방 저장 완료 - roomId: {}, savedStatus: {}", roomId, savedRoom.getRoomStatus());
        } catch (Exception e) {
            log.error("상담방 저장 중 오류", e);
            throw new RuntimeException("상담방 저장 실패", e);
        }
        
        // 5. 알림 전송 (실패해도 거절은 성공으로 처리)
        try {
            notificationService.sendStatusChangeNotification(savedRoom, "REJECTED");
            log.info("거절 알림 전송 완료 - roomUuid: {}", savedRoom.getRoomUuid());
        } catch (Exception e) {
            log.error("거절 알림 전송 실패 (무시하고 계속 진행)", e);
        }
        
        return savedRoom;
    }
    
    /**
     * ConsultationRoom을 DTO로 변환
     */
    @Transactional(readOnly = true)
    public ConsultationRoomDto toDto(ConsultationRoom room) {
        return ConsultationRoomDto.builder()
                .roomId(room.getRoomId())
                .roomUuid(room.getRoomUuid())
                .userId(room.getUser().getUserId())
                .userName(room.getUser().getNickname())
                .vetId(room.getVet().getVetId())
                .vetName(room.getVet().getUser().getNickname())
                .petId(room.getPet() != null ? room.getPet().getPetId() : null)
                .petName(room.getPet() != null ? room.getPet().getPetName() : null)
                .roomStatus(room.getRoomStatus())
                .consultationType(room.getConsultationType())
                .scheduledDatetime(room.getScheduledDatetime())
                .startedAt(room.getStartedAt())
                .endedAt(room.getEndedAt())
                .durationMinutes(room.getDurationMinutes())
                .consultationFee(room.getConsultationFee())
                .paymentStatus(room.getPaymentStatus())
                .paymentMethod(room.getPaymentMethod())
                .chiefComplaint(room.getChiefComplaint())
                .consultationNotes(room.getConsultationNotes())
                .prescription(room.getPrescription())
                .createdAt(room.getCreatedAt())
                .build();
    }
}