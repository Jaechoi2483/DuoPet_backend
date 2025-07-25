package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.model.dto.ConsultationRoomDto;
import com.petlogue.duopetbackend.consultation.model.dto.CreateConsultationDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationRoomService {
    
    private final ConsultationRoomRepository consultationRoomRepository;
    private final UserRepository userRepository;
    private final VetRepository vetRepository;
    private final PetRepository petRepository;
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
        
        // 즉시 상담인 경우 수의사에게 알림 전송
        if (schedule == null && "CHAT".equals(dto.getConsultationType())) {
            notificationService.sendNewConsultationNotification(savedRoom);
        }
        
        return savedRoom;
    }
    
    /**
     * 상담방 조회 (UUID)
     */
    public ConsultationRoom getConsultationRoomByUuid(String roomUuid) {
        return consultationRoomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
    }
    
    /**
     * 사용자의 상담 목록 조회
     */
    public Page<ConsultationRoom> getUserConsultations(Long userId, Pageable pageable) {
        return consultationRoomRepository.findByUserId(userId, pageable);
    }
    
    /**
     * 수의사의 상담 목록 조회
     */
    public Page<ConsultationRoom> getVetConsultations(Long vetId, Pageable pageable) {
        return consultationRoomRepository.findByVetId(vetId, pageable);
    }
    
    /**
     * 오늘의 예약 상담 조회 (수의사용)
     */
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
        
        // 상태 검증
        if (!"CREATED".equals(room.getRoomStatus()) && !"WAITING".equals(room.getRoomStatus())) {
            throw new IllegalStateException("대기 중인 상담만 승인할 수 있습니다. 현재 상태: " + room.getRoomStatus());
        }
        
        // 상태 변경 - APPROVED 상태가 없는 경우 IN_PROGRESS로 변경
        room.setRoomStatus("IN_PROGRESS");
        room.setStartedAt(LocalDateTime.now());
        
        // 저장
        ConsultationRoom savedRoom = consultationRoomRepository.save(room);
        
        // 승인 알림 전송 (사용자에게)
        notificationService.sendStatusChangeNotification(savedRoom, "APPROVED");
        
        log.info("Consultation room {} approved by vet {}", roomId, room.getVet().getVetId());
        
        return savedRoom;
    }

    /**
     * 상담 거절
     */
    @Transactional
    public ConsultationRoom rejectConsultation(Long roomId) {
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("상담방을 찾을 수 없습니다."));
        
        // 상태 검증
        if (!"CREATED".equals(room.getRoomStatus()) && !"WAITING".equals(room.getRoomStatus())) {
            throw new IllegalStateException("대기 중인 상담만 거절할 수 있습니다. 현재 상태: " + room.getRoomStatus());
        }
        
        // 상태 변경 - REJECTED 상태가 없는 경우 CANCELLED로 변경
        room.setRoomStatus("CANCELLED");
        room.setEndedAt(LocalDateTime.now());
        room.setConsultationNotes("상담이 전문가에 의해 거절되었습니다.");
        
        // 저장
        ConsultationRoom savedRoom = consultationRoomRepository.save(room);
        
        // 거절 알림 전송 (사용자에게)
        notificationService.sendStatusChangeNotification(savedRoom, "REJECTED");
        
        log.info("Consultation room {} rejected by vet {}", roomId, room.getVet().getVetId());
        
        return savedRoom;
    }
    
    /**
     * ConsultationRoom을 DTO로 변환
     */
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