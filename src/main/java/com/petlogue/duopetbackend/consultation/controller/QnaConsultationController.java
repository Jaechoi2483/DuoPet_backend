package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.common.response.ApiResponse;
import com.petlogue.duopetbackend.common.response.ResponseUtil;
import com.petlogue.duopetbackend.consultation.model.dto.QnaConsultationDto;
import com.petlogue.duopetbackend.consultation.model.service.QnaConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/consultation/qna")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class QnaConsultationController {
    
    private final QnaConsultationService qnaConsultationService;
    
    // Q&A 상담 생성
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createQnaConsultation(
            @RequestAttribute("userId") Long userId,
            @RequestParam("vetId") Long vetId,
            @RequestParam("petId") Long petId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            log.info("Q&A 상담 생성 요청 - userId: {}, vetId: {}, petId: {}", userId, vetId, petId);
            
            QnaConsultationDto.CreateRequest createRequest = QnaConsultationDto.CreateRequest.builder()
                    .vetId(vetId)
                    .petId(petId)
                    .title(title)
                    .content(content)
                    .category(category != null ? category : "기타")
                    .files(files)
                    .build();
            
            QnaConsultationDto.Response response = qnaConsultationService.createQnaConsultation(userId, createRequest);
            
            return ResponseEntity.ok(ResponseUtil.success("Q&A 상담이 성공적으로 등록되었습니다.", response));
        } catch (Exception e) {
            log.error("Q&A 상담 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("Q&A 상담 등록에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // Q&A 답변 작성 (수의사용)
    @PostMapping("/{roomId}/answer")
    public ResponseEntity<ApiResponse<?>> createAnswer(
            @RequestAttribute("userId") Long vetId,
            @PathVariable Long roomId,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            log.info("Q&A 답변 작성 요청 - vetId: {}, roomId: {}", vetId, roomId);
            
            QnaConsultationDto.AnswerRequest answerRequest = QnaConsultationDto.AnswerRequest.builder()
                    .content(content)
                    .files(files)
                    .build();
            
            QnaConsultationDto.Response response = qnaConsultationService.createAnswer(vetId, roomId, answerRequest);
            
            return ResponseEntity.ok(ResponseUtil.success("답변이 성공적으로 등록되었습니다.", response));
        } catch (Exception e) {
            log.error("답변 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("답변 등록에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // 내 Q&A 상담 목록 조회 (사용자/수의사 공통)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyQnaConsultations(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "role", required = false) String role) {
        
        try {
            log.info("Q&A 상담 목록 조회 - userId: {}, role: {}", userId, role);
            Pageable pageable = PageRequest.of(page, size);
            
            Page<QnaConsultationDto.Response> consultations;
            
            // role 파라미터로 사용자/수의사 구분
            if ("vet".equals(role)) {
                consultations = qnaConsultationService.getVetQnaConsultations(userId, null, pageable);
            } else {
                consultations = qnaConsultationService.getUserQnaConsultations(userId, pageable);
            }
            
            log.info("Q&A 상담 목록 조회 결과 - 총 {}건", consultations.getTotalElements());
            
            // 첫 번째 항목의 데이터 확인
            if (!consultations.isEmpty()) {
                QnaConsultationDto.Response firstItem = consultations.getContent().get(0);
                log.info("첫 번째 Q&A 상담 데이터: roomId={}, title={}, createdAt={}", 
                        firstItem.getRoomId(), firstItem.getTitle(), firstItem.getCreatedAt());
            }
            
            return ResponseEntity.ok(ResponseUtil.success("Q&A 상담 목록을 조회했습니다.", consultations));
        } catch (Exception e) {
            log.error("Q&A 상담 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("Q&A 상담 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // 수의사의 상태별 Q&A 상담 목록 조회
    @GetMapping("/vet/status/{status}")
    public ResponseEntity<ApiResponse<?>> getVetQnaConsultationsByStatus(
            @RequestAttribute("userId") Long vetId,
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            log.info("수의사 상태별 Q&A 상담 목록 조회 - vetId: {}, status: {}", vetId, status);
            Pageable pageable = PageRequest.of(page, size);
            
            Page<QnaConsultationDto.Response> consultations = 
                    qnaConsultationService.getVetQnaConsultations(vetId, status, pageable);
            
            return ResponseEntity.ok(ResponseUtil.success("상태별 Q&A 상담 목록을 조회했습니다.", consultations));
        } catch (Exception e) {
            log.error("상태별 Q&A 상담 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("상태별 Q&A 상담 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // Q&A 상담 상세 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<?>> getQnaConsultationDetail(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long roomId) {
        
        try {
            log.info("Q&A 상담 상세 조회 - userId: {}, roomId: {}", userId, roomId);
            
            QnaConsultationDto.Response response = 
                    qnaConsultationService.getQnaConsultationDetail(roomId, userId);
            
            log.info("Q&A 상담 상세 응답 데이터: {}", response);
            
            return ResponseEntity.ok(ResponseUtil.success("Q&A 상담 상세 정보를 조회했습니다.", response));
        } catch (Exception e) {
            log.error("Q&A 상담 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("Q&A 상담 상세 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // 추가 메시지 작성 (질문자의 추가 질문 또는 수의사의 추가 답변)
    @PostMapping("/{roomId}/message")
    public ResponseEntity<ApiResponse<?>> addMessage(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long roomId,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            log.info("추가 메시지 작성 - userId: {}, roomId: {}", userId, roomId);
            
            // 추가 메시지 작성 로직 구현 필요
            // 현재는 기본 응답만 반환
            return ResponseEntity.ok(ResponseUtil.success("추가 메시지 기능은 준비 중입니다.", null));
        } catch (Exception e) {
            log.error("추가 메시지 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ResponseUtil.error("추가 메시지 작성에 실패했습니다: " + e.getMessage()));
        }
    }
}