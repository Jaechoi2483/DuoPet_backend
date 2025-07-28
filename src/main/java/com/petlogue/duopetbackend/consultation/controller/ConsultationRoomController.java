package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.consultation.model.dto.ApiResponse;
import com.petlogue.duopetbackend.consultation.model.dto.ConsultationRoomDto;
import com.petlogue.duopetbackend.consultation.model.dto.CreateConsultationDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.model.service.ConsultationRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/consultation/rooms")
@RequiredArgsConstructor
public class ConsultationRoomController {
    
    private final ConsultationRoomService consultationRoomService;
    
    /**
     * 상담방 생성
     * POST /api/consultation/rooms
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConsultationRoomDto>> createRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateConsultationDto createDto) {
        
        try {
            // TODO: Get userId from authenticated user
            // createDto.setUserId(getUserIdFromAuth(userDetails));
            
            ConsultationRoom room = consultationRoomService.createConsultationRoom(createDto);
            ConsultationRoomDto roomDto = consultationRoomService.toDto(room);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("상담방이 생성되었습니다.", roomDto));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating consultation room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("상담방 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 상담방 조회 (UUID)
     * GET /api/consultation/rooms/{roomUuid}
     */
    @GetMapping("/{roomUuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConsultationRoomDto>> getRoom(
            @PathVariable String roomUuid,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            ConsultationRoom room = consultationRoomService.getConsultationRoomByUuid(roomUuid);
            
            // TODO: Verify user has access to this room
            
            ConsultationRoomDto roomDto = consultationRoomService.toDto(room);
            return ResponseEntity.ok(ApiResponse.success(roomDto));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("상담방을 찾을 수 없습니다."));
        }
    }
    
    /**
     * 내 상담 목록 조회 (사용자)
     * GET /api/consultation/rooms/my-consultations
     */
    @GetMapping("/my-consultations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ConsultationRoomDto>>> getMyConsultations(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // TODO: Get userId from authenticated user
        Long userId = 1L; // Placeholder
        
        Page<ConsultationRoom> rooms = consultationRoomService.getUserConsultations(userId, pageable);
        Page<ConsultationRoomDto> roomDtos = rooms.map(consultationRoomService::toDto);
        
        return ResponseEntity.ok(ApiResponse.success(roomDtos));
    }
    
    /**
     * 수의사 상담 목록 조회
     * GET /api/consultation/rooms/vet/{vetId}/consultations
     */
    @GetMapping("/vet/{vetId}/consultations")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Page<ConsultationRoomDto>>> getVetConsultations(
            @PathVariable Long vetId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // TODO: Verify authenticated user is the vet
        
        Page<ConsultationRoom> rooms = consultationRoomService.getVetConsultations(vetId, pageable);
        Page<ConsultationRoomDto> roomDtos = rooms.map(consultationRoomService::toDto);
        
        return ResponseEntity.ok(ApiResponse.success(roomDtos));
    }
    
    /**
     * 오늘의 예약 상담 조회 (수의사용)
     * GET /api/consultation/rooms/vet/{vetId}/today-schedule
     */
    @GetMapping("/vet/{vetId}/today-schedule")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<List<ConsultationRoomDto>>> getTodaySchedule(
            @PathVariable Long vetId) {
        
        List<ConsultationRoom> rooms = consultationRoomService.getTodayScheduledConsultations(vetId);
        List<ConsultationRoomDto> roomDtos = rooms.stream()
                .map(consultationRoomService::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(roomDtos));
    }
    
    /**
     * 진행 중인 상담방 조회
     * GET /api/consultation/rooms/active
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ConsultationRoomDto>>> getActiveRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // TODO: Get userId from authenticated user
        Long userId = 1L; // Placeholder
        
        List<ConsultationRoom> rooms = consultationRoomService.getActiveRooms(userId);
        List<ConsultationRoomDto> roomDtos = rooms.stream()
                .map(consultationRoomService::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(roomDtos));
    }
    
    /**
     * 상담 시작
     * POST /api/consultation/rooms/{roomId}/start
     */
    @PostMapping("/{roomId}/start")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> startConsultation(
            @PathVariable Long roomId) {
        
        try {
            consultationRoomService.startConsultation(roomId);
            return ResponseEntity.ok(ApiResponse.success("상담이 시작되었습니다.", null));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 상담 종료
     * POST /api/consultation/rooms/{roomId}/end
     */
    @PostMapping("/{roomId}/end")
    @PreAuthorize("hasAnyAuthority('VET', 'user')")
    public ResponseEntity<ApiResponse<Void>> endConsultation(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // 로그인한 사용자가 해당 상담의 참여자인지 확인
            if (userDetails != null) {
                String username = userDetails.getUsername();
                ConsultationRoom room = consultationRoomService.getConsultationById(roomId);
                
                // 상담 참여자 확인 (일반 사용자 또는 수의사)
                boolean isParticipant = false;
                if (room.getUser() != null && username.equals(room.getUser().getLoginId())) {
                    isParticipant = true;
                } else if (room.getVet() != null && room.getVet().getUser() != null && 
                          username.equals(room.getVet().getUser().getLoginId())) {
                    isParticipant = true;
                }
                
                if (!isParticipant) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("상담 참여자만 종료할 수 있습니다."));
                }
            }
            
            consultationRoomService.endConsultation(roomId);
            return ResponseEntity.ok(ApiResponse.success("상담이 종료되었습니다.", null));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("상담 종료 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("상담 종료 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 상담 취소
     * POST /api/consultation/rooms/{roomId}/cancel
     */
    @PostMapping("/{roomId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancelConsultation(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request) {
        
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("취소 사유를 입력해주세요."));
            }
            
            consultationRoomService.cancelConsultation(roomId, reason);
            return ResponseEntity.ok(ApiResponse.success("상담이 취소되었습니다.", null));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 상담 노트 업데이트
     * PUT /api/consultation/rooms/{roomId}/notes
     */
    @PutMapping("/{roomId}/notes")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> updateNotes(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request) {
        
        try {
            String notes = request.get("notes");
            consultationRoomService.updateConsultationNotes(roomId, notes);
            return ResponseEntity.ok(ApiResponse.success("상담 노트가 업데이트되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 처방전 업데이트
     * PUT /api/consultation/rooms/{roomId}/prescription
     */
    @PutMapping("/{roomId}/prescription")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> updatePrescription(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request) {
        
        try {
            String prescription = request.get("prescription");
            consultationRoomService.updatePrescription(roomId, prescription);
            return ResponseEntity.ok(ApiResponse.success("처방전이 업데이트되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 결제 완료 처리
     * POST /api/consultation/rooms/{roomId}/payment
     */
    @PostMapping("/{roomId}/payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsPaid(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request) {
        
        try {
            String paymentMethod = request.get("paymentMethod");
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("결제 방법을 선택해주세요."));
            }
            
            consultationRoomService.markAsPaid(roomId, paymentMethod);
            return ResponseEntity.ok(ApiResponse.success("결제가 완료되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 즉시 상담 승인
     * PUT /api/consultation/rooms/{roomId}/approve
     */
    @PutMapping("/{roomId}/approve")
    @PreAuthorize("hasAnyAuthority('VET', 'user')")  // user 권한도 허용
    public ResponseEntity<ApiResponse<ConsultationRoomDto>> approveConsultation(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            ConsultationRoom approvedRoom = consultationRoomService.approveConsultation(roomId);
            ConsultationRoomDto roomDto = consultationRoomService.toDto(approvedRoom);
            
            return ResponseEntity.ok(ApiResponse.success("상담이 승인되었습니다.", roomDto));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("상담방을 찾을 수 없습니다."));
        }
    }

    /**
     * 즉시 상담 거절
     * PUT /api/consultation/rooms/{roomId}/reject
     */
    @PutMapping("/{roomId}/reject")
    @PreAuthorize("hasAnyAuthority('VET', 'user')")  // user 권한도 허용
    public ResponseEntity<ApiResponse<ConsultationRoomDto>> rejectConsultation(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("상담 거절 요청 - roomId: {}, vet: {}", roomId, 
                userDetails != null ? userDetails.getUsername() : "Unknown");
        
        try {
            ConsultationRoom rejectedRoom = consultationRoomService.rejectConsultation(roomId);
            ConsultationRoomDto roomDto = consultationRoomService.toDto(rejectedRoom);
            
            log.info("상담 거절 완료 - roomId: {}, status: {}", roomId, rejectedRoom.getRoomStatus());
            
            return ResponseEntity.ok(ApiResponse.success("상담이 거절되었습니다.", roomDto));
            
        } catch (IllegalStateException e) {
            log.error("상담 거절 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("상담 거절 실패 - 상담방 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("상담방을 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("상담 거절 실패 - 예외 발생: ", e);
            // 스택 트레이스 전체 출력
            e.printStackTrace();
            
            String errorMessage = "상담 거절 처리 중 오류가 발생했습니다.";
            if (e.getMessage() != null) {
                errorMessage += " (" + e.getMessage() + ")";
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(errorMessage));
        }
    }
}