package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.common.response.ApiResponse;
import com.petlogue.duopetbackend.common.response.ResponseUtil;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/consultation/debug")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class QnaDebugController {
    
    private final ConsultationRoomRepository consultationRoomRepository;
    
    // Q&A 상담 데이터 디버깅용 엔드포인트
    @GetMapping("/qna/{userId}")
    public ResponseEntity<ApiResponse<?>> debugQnaConsultations(@PathVariable Long userId) {
        try {
            // 1. 모든 QNA 타입 상담 조회
            List<ConsultationRoom> allQna = consultationRoomRepository.findAll().stream()
                    .filter(room -> "QNA".equals(room.getConsultationType()))
                    .toList();
            
            // 2. 특정 사용자의 QNA 상담 조회
            List<ConsultationRoom> userQna = consultationRoomRepository.findAll().stream()
                    .filter(room -> "QNA".equals(room.getConsultationType()) 
                            && room.getUser().getUserId().equals(userId))
                    .toList();
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("totalQnaConsultations", allQna.size());
            debugInfo.put("userQnaConsultations", userQna.size());
            debugInfo.put("requestedUserId", userId);
            debugInfo.put("allQnaData", allQna.stream().map(room -> {
                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("roomId", room.getRoomId());
                roomInfo.put("userId", room.getUser().getUserId());
                roomInfo.put("userName", room.getUser().getUserName());
                roomInfo.put("consultationType", room.getConsultationType());
                roomInfo.put("roomStatus", room.getRoomStatus());
                roomInfo.put("createdAt", room.getCreatedAt());
                return roomInfo;
            }).toList());
            debugInfo.put("userQnaData", userQna.stream().map(room -> {
                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("roomId", room.getRoomId());
                roomInfo.put("title", room.getChiefComplaint());
                roomInfo.put("status", room.getRoomStatus());
                roomInfo.put("createdAt", room.getCreatedAt());
                return roomInfo;
            }).toList());
            
            return ResponseEntity.ok(ResponseUtil.success("Q&A 상담 디버그 정보", debugInfo));
        } catch (Exception e) {
            log.error("디버그 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("디버그 조회 실패: " + e.getMessage()));
        }
    }
    
    // 모든 상담 타입 확인
    @GetMapping("/consultation-types")
    public ResponseEntity<ApiResponse<?>> getAllConsultationTypes() {
        try {
            List<String> types = consultationRoomRepository.findAll().stream()
                    .map(ConsultationRoom::getConsultationType)
                    .distinct()
                    .toList();
            
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("distinctTypes", types);
            typeInfo.put("typeCount", types.stream().map(type -> {
                Map<String, Object> count = new HashMap<>();
                count.put("type", type);
                count.put("count", consultationRoomRepository.findAll().stream()
                        .filter(room -> type.equals(room.getConsultationType()))
                        .count());
                return count;
            }).toList());
            
            return ResponseEntity.ok(ResponseUtil.success("상담 타입 정보", typeInfo));
        } catch (Exception e) {
            log.error("타입 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("타입 조회 실패: " + e.getMessage()));
        }
    }
}