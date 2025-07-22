package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
import com.petlogue.duopetbackend.board.jpa.repository.LikeRepository;
import com.petlogue.duopetbackend.board.jpa.repository.ReportRepository;
import com.petlogue.duopetbackend.board.model.dto.Comments;
import com.petlogue.duopetbackend.board.model.dto.Report;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final LikeRepository likeRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 게시물 번호를 기준으로 댓글 + 대댓글 전체 조회
    public ArrayList<Comments> selectCommentList(Long contentId) {
        List<CommentsEntity> entityList = commentsRepository
                .findByContentIdOrderByParentCommentIdAscCreatedAtAsc(contentId);

        ArrayList<Comments> list = new ArrayList<>();
        for (CommentsEntity entity : entityList) {
            list.add(entity.toDto()); // 닉네임 포함된 DTO로 변환
        }
        return list;
    }

    // 댓글 등록
    public Object insertComment(Comments dto) {
        try {
            // userId로 UserEntity 조회
            UserEntity user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

            // user를 주입해서 Entity 생성
            CommentsEntity entity = dto.toEntity(user);
            // 저장
            CommentsEntity saved = commentsRepository.save(entity);
            // DTO로 변환해서 반환
            return saved.toDto();
        } catch (Exception e) {
            log.error("댓글 등록 실패", e);
            return null;
        }
    }

    // 댓글 삭제
    public int deleteComment(Long commentId) {
        try {
            commentsRepository.deleteById(commentId);
            return 1;
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            return 0;
        }
    }

        // 댓글 좋아요 토글
        public int toggleCommentLike(Long commentId, Long userId) {
            boolean alreadyLiked = likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, commentId, "comment");

            if (alreadyLiked) {
                // 좋아요 취소
                likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, commentId, "comment");
                commentsRepository.decrementLikeCount(commentId);
                log.info("댓글 좋아요 취소: userId={}, commentId={}", userId, commentId);
            } else {
                // 좋아요 추가
                LikeEntity like = new LikeEntity(userId, commentId, "comment");
                likeRepository.save(like);
                commentsRepository.incrementLikeCount(commentId);
                log.info("댓글 좋아요 추가: userId={}, commentId={}", userId, commentId);
            }
            return commentsRepository.findLikeCountByCommentId(commentId);
        }

        // 댓글 신고
        public void reportComment(Long commentId, Long userId, Report dto) {
            dto.setTargetId(commentId);
            dto.setTargetType("comment");
            dto.setUserId(userId);

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            reportRepository.save(dto.toReportEntity(user)); // toEntity 메서드 필요시 추가
            commentsRepository.incrementReportCount(commentId);
    }
}
