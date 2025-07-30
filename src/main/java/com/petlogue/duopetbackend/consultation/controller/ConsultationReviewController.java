package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.consultation.model.dto.ApiResponse;
import com.petlogue.duopetbackend.consultation.model.dto.ConsultationReviewDto;
import com.petlogue.duopetbackend.consultation.model.dto.ReviewStatisticsDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationReview;
import com.petlogue.duopetbackend.consultation.model.service.ConsultationReviewService;
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

@Slf4j
@RestController
@RequestMapping("/api/consultation/reviews")
@RequiredArgsConstructor
public class ConsultationReviewController {
    
    private final ConsultationReviewService reviewService;
    
    /**
     * 리뷰 작성
     * POST /api/consultation/reviews/room/{roomId}
     */
    @PostMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<ConsultationReviewDto>> createReview(
            @PathVariable Long roomId,
            @Valid @RequestBody ConsultationReviewDto reviewDto,
            @RequestAttribute("userId") Long userId) {
        
        try {
            log.info("리뷰 작성 요청 - roomId: {}, userId: {}", roomId, userId);
            
            // userId를 reviewDto에 설정
            reviewDto.setUserId(userId);
            
            ConsultationReview review = reviewService.createReview(roomId, reviewDto);
            
            log.info("리뷰 생성 완료 - reviewId: {}", review.getReviewId());
            
            // 엔티티를 DTO로 변환하여 반환
            ConsultationReviewDto responseDto = ConsultationReviewDto.builder()
                    .reviewId(review.getReviewId())
                    .consultationRoomId(review.getConsultationRoom().getRoomId())
                    .userId(review.getUser().getUserId())
                    .userName(review.getUser().getNickname())
                    .vetId(review.getVet().getVetId())
                    .vetName(review.getVet().getName())  // VetEntity의 name 필드 직접 사용
                    .rating(review.getRating())
                    .kindnessScore(review.getKindnessScore())
                    .professionalScore(review.getProfessionalScore())
                    .responseScore(review.getResponseScore())
                    .reviewContent(review.getReviewContent())
                    .isVisible("Y".equals(review.getIsVisible()))
                    .createdAt(review.getCreatedAt())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("리뷰가 작성되었습니다.", responseDto));
            
        } catch (IllegalArgumentException e) {
            log.error("리뷰 작성 실패 - IllegalArgumentException: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("리뷰 작성 실패 - IllegalStateException: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 작성 실패 - 예상치 못한 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 작성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 수의사 답변 작성
     * POST /api/consultation/reviews/{reviewId}/reply
     */
    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ApiResponse<Void>> addVetReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request,
            @RequestAttribute("userId") Long userId) {
        
        try {
            String reply = request.get("reply");
            if (reply == null || reply.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("답변 내용을 입력해주세요."));
            }
            
            // userId로 vetId 조회 (수의사의 userId로 vetId를 찾음)
            // 이 부분은 VetRepository를 주입받아 처리해야 함
            Long vetId = 1L; // TODO: vetRepository.findByUser_UserId(userId).getVetId()
            
            reviewService.addVetReply(reviewId, vetId, reply);
            return ResponseEntity.ok(ApiResponse.success("답변이 등록되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 수의사별 리뷰 목록 조회
     * GET /api/consultation/reviews/vet/{vetId}
     */
    @GetMapping("/vet/{vetId}")
    public ResponseEntity<ApiResponse<Page<ConsultationReviewDto>>> getVetReviews(
            @PathVariable Long vetId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ConsultationReviewDto> reviews = reviewService.getVetReviews(vetId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
    
    /**
     * 사용자가 작성한 리뷰 목록 조회
     * GET /api/consultation/reviews/my-reviews
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ConsultationReviewDto>>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // TODO: Get userId from authenticated user
        Long userId = 1L; // Placeholder
        
        List<ConsultationReviewDto> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
    
    /**
     * 수의사 평점 통계 조회
     * GET /api/consultation/reviews/vet/{vetId}/statistics
     */
    @GetMapping("/vet/{vetId}/statistics")
    public ResponseEntity<ApiResponse<ReviewStatisticsDto>> getVetStatistics(
            @PathVariable Long vetId) {
        
        ReviewStatisticsDto statistics = reviewService.getVetReviewStatistics(vetId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    
    /**
     * 답변이 없는 리뷰 조회 (수의사용)
     * GET /api/consultation/reviews/vet/{vetId}/unanswered
     */
    @GetMapping("/vet/{vetId}/unanswered")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<List<ConsultationReviewDto>>> getUnansweredReviews(
            @PathVariable Long vetId) {
        
        List<ConsultationReviewDto> reviews = reviewService.getUnansweredReviews(vetId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
    
    /**
     * 리뷰 숨김/표시 토글
     * PATCH /api/consultation/reviews/{reviewId}/visibility
     */
    @PatchMapping("/{reviewId}/visibility")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> toggleVisibility(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Get userId from authenticated user
            Long userId = 1L; // Placeholder
            
            reviewService.toggleReviewVisibility(reviewId, userId);
            return ResponseEntity.ok(ApiResponse.success("리뷰 공개 상태가 변경되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}