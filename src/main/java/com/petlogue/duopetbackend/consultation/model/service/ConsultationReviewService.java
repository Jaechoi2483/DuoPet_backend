package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.model.dto.ConsultationReviewDto;
import com.petlogue.duopetbackend.consultation.model.dto.ReviewStatisticsDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationReview;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationReviewRepository;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository;
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
        
        room.setReview(review);
        
        ConsultationReview savedReview = reviewRepository.save(review);
        log.info("Review created for consultation room {}", roomId);
        
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
                .vetName(review.getVet().getUser().getNickname())
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