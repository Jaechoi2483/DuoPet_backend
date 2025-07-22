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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConsultationReview>> createReview(
            @PathVariable Long roomId,
            @Valid @RequestBody ConsultationReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // TODO: Get userId from authenticated user
            reviewDto.setUserId(1L); // Placeholder
            
            ConsultationReview review = reviewService.createReview(roomId, reviewDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("리뷰가 작성되었습니다.", review));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 수의사 답변 작성
     * POST /api/consultation/reviews/{reviewId}/reply
     */
    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> addVetReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            String reply = request.get("reply");
            if (reply == null || reply.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("답변 내용을 입력해주세요."));
            }
            
            // TODO: Get vetId from authenticated user
            Long vetId = 1L; // Placeholder
            
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