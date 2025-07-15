package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
import com.petlogue.duopetbackend.board.jpa.repository.LikeRepository;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.petlogue.duopetbackend.board.model.dto.Like;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LikeService {
    private final LikeRepository LikeRepository;
    private final BoardRepository boardRepository;

    private static final String TARGET_TYPE = "board";

    @Transactional
    public Like toggleLike(Long userId, Long contentId) {
        // 중복 체크
        Optional<LikeEntity> existing = LikeRepository
                .findByUserIdAndTargetIdAndTargetType(userId, contentId, TARGET_TYPE);

        // 게시글 조회
        BoardEntity board = boardRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        boolean liked;

        if (existing.isPresent()) {
            // 좋아요 취소
            LikeRepository.delete(existing.get());
            board.setLikeCount(board.getLikeCount() - 1);
            liked = false;
        } else {
            // 좋아요 등록
            LikeEntity newLike = LikeEntity.builder()
                    .userId(userId)
                    .targetId(contentId)
                    .targetType(TARGET_TYPE)
                    .createdAt(new Date())
                    .build();

            LikeRepository.save(newLike);
            board.setLikeCount(board.getLikeCount() + 1);
            liked = true;
        }

        // 반환 DTO
        return Like.builder()
                .userId(userId)
                .targetId(contentId)
                .targetType("board")
                .createdAt(new Date())
                .liked(liked)
                .build();
    }
}
