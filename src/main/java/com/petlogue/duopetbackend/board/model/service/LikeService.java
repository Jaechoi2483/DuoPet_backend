package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.LikeRepository;
import com.petlogue.duopetbackend.board.model.dto.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;

    public Like toggleLike(Long userId, Long boardId) {
        String targetType = "board";

        Optional<LikeEntity> existing = likeRepository.findByUserIdAndTargetIdAndTargetType(userId, boardId, targetType);

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (existing.isPresent()) {
            likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, boardId, targetType);
            board.setLikeCount(Math.max(board.getLikeCount() - 1, 0)); // 음수 방지
            boardRepository.save(board);
            log.info("좋아요 취소 완료 - contentId: {}, userId: {}", boardId, userId);
            return new Like(boardId, false);
        } else {
            LikeEntity newLike = LikeEntity.builder()
                    .userId(userId)
                    .targetId(boardId)
                    .targetType(targetType)
                    .build();
            likeRepository.save(newLike);
            board.setLikeCount(board.getLikeCount() + 1);
            boardRepository.save(board);
            log.info("좋아요 등록 완료 - contentId: {}, userId: {}", boardId, userId);
            return new Like(boardId, true);
        }
    }
}