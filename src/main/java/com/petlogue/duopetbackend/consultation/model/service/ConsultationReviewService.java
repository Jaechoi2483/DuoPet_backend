package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.model.dto.ConsultationReviewDto;
import com.petlogue.duopetbackend.consultation.model.dto.ReviewStatisticsDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationReview;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetProfile;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationReviewRepository;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository;
import com.petlogue.duopetbackend.consultation.jpa.repository.VetProfileRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationReviewService {
    
    private final ConsultationReviewRepository reviewRepository;
    private final ConsultationRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final VetRepository vetRepository;
    private final VetProfileRepository vetProfileRepository;
    
    /**
     * 리뷰 작성
     */
    @Transactional
    public ConsultationReview createReview(Long roomId, ConsultationReviewDto dto) {
        // Validate consultation room
        ConsultationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation room not found"));
        
        // Check if consultation is completed
        if (!"COMPLETED".equals(room.getRoomStatus())) {
            throw new IllegalStateException("Cannot review uncompleted consultation");
        }
        
        // Check if review already exists
        if (reviewRepository.findByConsultationRoom_RoomId(roomId).isPresent()) {
            throw new IllegalStateException("Review already exists for this consultation");
        }
        
        // Validate user is the consultation owner
        if (!room.getUser().getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("User is not authorized to review this consultation");
        }
        
        UserEntity user = room.getUser();
        VetEntity vet = room.getVet();
        
        ConsultationReview review = ConsultationReview.builder()
                .consultationRoom(room)
                .user(user)
                .vet(vet)
                .rating(dto.getRating())
                .kindnessScore(dto.getKindnessScore())
                .professionalScore(dto.getProfessionalScore())
                .responseScore(dto.getResponseScore())
                .reviewContent(dto.getReviewContent())
                .isVisible("Y")
                .build();
        
        // room.setReview(review); // 양방향 관계 설정 제거 - 순환 참조 방지
        
        ConsultationReview savedReview = reviewRepository.save(review);
        reviewRepository.flush(); // IDENTITY 전략 사용 시 ID 즉시 생성을 위해 flush
        
        log.info("Review created for consultation room {} with rating {}", roomId, dto.getRating());
        
        // 리뷰 저장 확인
        log.info("Saved review ID: {}, vet ID: {}, user ID: {}", savedReview.getReviewId(), vet.getVetId(), user.getUserId());
        
        // VetProfile 평점 통계 업데이트 전 상태 확인
        VetProfile vetProfileBefore = vetProfileRepository.findByVet_VetId(vet.getVetId())
                .orElse(null);
        if (vetProfileBefore != null) {
            log.info("Before update - vet {} rating: {}, count: {}", 
                    vet.getVetId(), vetProfileBefore.getRatingAvg(), vetProfileBefore.getRatingCount());
        }
        
        // VetProfile 평점 통계 업데이트 (전체 리뷰 기반으로 재계산)
        try {
            vetProfileRepository.updateRatingStatistics(vet.getVetId());
            log.info("Called updateRatingStatistics for vet {}", vet.getVetId());
            
            // 강제로 flush하여 즉시 DB에 반영
            vetProfileRepository.flush();
        } catch (Exception e) {
            log.error("Error updating rating statistics for vet {}: {}", vet.getVetId(), e.getMessage(), e);
        }
        
        // 업데이트된 평점 로그 출력
        VetProfile vetProfileAfter = vetProfileRepository.findByVet_VetId(vet.getVetId())
                .orElse(null);
        if (vetProfileAfter != null) {
            log.info("After update - vet {} rating: {}, count: {}", 
                    vet.getVetId(), vetProfileAfter.getRatingAvg(), vetProfileAfter.getRatingCount());
            
            // 항상 수동으로 계산 (updateRatingStatistics가 작동하지 않는 문제 해결)
            if (true) {  // 항상 실행
                log.info("Calculating rating manually for vet {}", vet.getVetId());
                
                // 해당 수의사의 모든 리뷰 가져오기
                List<ConsultationReview> vetReviews = reviewRepository.findByVetIdAndIsVisible(vet.getVetId(), "Y");
                if (!vetReviews.isEmpty()) {
                    double avgRating = vetReviews.stream()
                            .mapToInt(ConsultationReview::getRating)
                            .average()
                            .orElse(0.0);
                    
                    vetProfileAfter.setRatingAvg(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
                    vetProfileAfter.setRatingCount(vetReviews.size());
                    
                    vetProfileRepository.save(vetProfileAfter);
                    log.info("Manually updated vet {} rating to: {}, count: {}", 
                            vet.getVetId(), vetProfileAfter.getRatingAvg(), vetProfileAfter.getRatingCount());
                }
            }
        }
        
        return savedReview;
    }
    
    /**
     * 수의사 답변 추가
     */
    @Transactional
    public void addVetReply(Long reviewId, Long vetId, String reply) {
        ConsultationReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Validate vet is the consultation vet
        if (!review.getVet().getVetId().equals(vetId)) {
            throw new IllegalArgumentException("Vet is not authorized to reply to this review");
        }
        
        review.setVetReply(reply);
        
        reviewRepository.save(review);
        log.info("Vet reply added to review {}", reviewId);
    }
    
    /**
     * 수의사별 리뷰 목록 조회
     */
    public Page<ConsultationReviewDto> getVetReviews(Long vetId, Pageable pageable) {
        Page<ConsultationReview> reviews = reviewRepository.findByVetId(vetId, pageable);
        
        return reviews.map(this::toDto);
    }
    
    /**
     * 사용자가 작성한 리뷰 목록 조회
     */
    public List<ConsultationReviewDto> getUserReviews(Long userId) {
        List<ConsultationReview> reviews = reviewRepository.findByUserId(userId);
        
        return reviews.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 수의사 평점 통계 조회
     */
    public ReviewStatisticsDto getVetReviewStatistics(Long vetId) {
        Object[] stats = reviewRepository.calculateDetailedStatistics(vetId);
        
        if (stats == null || stats.length == 0) {
            return ReviewStatisticsDto.builder()
                    .averageRating(0.0)
                    .averageKindnessScore(0.0)
                    .averageProfessionalScore(0.0)
                    .averageResponseScore(0.0)
                    .totalReviews(0)
                    .build();
        }
        
        // stats is already an Object[] array, not a nested array
        return ReviewStatisticsDto.builder()
                .averageRating(stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0)
                .averageKindnessScore(stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0)
                .averageProfessionalScore(stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0)
                .averageResponseScore(stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0)
                .totalReviews(stats[4] != null ? ((Number) stats[4]).intValue() : 0)
                .build();
    }
    
    /**
     * 답변이 없는 리뷰 조회 (수의사용)
     */
    public List<ConsultationReviewDto> getUnansweredReviews(Long vetId) {
        List<ConsultationReview> reviews = reviewRepository.findUnansweredReviews(vetId);
        
        return reviews.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 리뷰 숨김/표시 처리
     */
    @Transactional
    public void toggleReviewVisibility(Long reviewId, Long userId) {
        ConsultationReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Only review owner can toggle visibility
        if (!review.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to modify this review");
        }
        
        review.setIsVisible("Y".equals(review.getIsVisible()) ? "N" : "Y");
        reviewRepository.save(review);
        
        log.info("Review {} visibility toggled to {}", reviewId, review.getIsVisible());
    }
    
    /**
     * ConsultationReview를 DTO로 변환
     */
    private ConsultationReviewDto toDto(ConsultationReview review) {
        return ConsultationReviewDto.builder()
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
                .vetReply(review.getVetReply())
                .isVisible("Y".equals(review.getIsVisible()))
                .createdAt(review.getCreatedAt())
                .build();
    }
}