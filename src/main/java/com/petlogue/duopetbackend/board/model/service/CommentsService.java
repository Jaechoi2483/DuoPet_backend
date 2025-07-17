package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
import com.petlogue.duopetbackend.board.model.dto.Comments;
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

    // 게시물 번호를 기준으로 댓글 + 대댓글 전체 조회
    public ArrayList<Comments> selectCommentList(Long contentId) {
        List<CommentsEntity> entityList = commentsRepository
                .findByContentIdOrderByParentCommentIdAscCreatedAtAsc(contentId);

        ArrayList<Comments> list = new ArrayList<>();
        for (CommentsEntity entity : entityList) {
            list.add(entity.toDto());
        }
        return list;
    }

    // 댓글 등록
    public Object insertComment(Comments dto) {
        try {
            CommentsEntity entity = dto.toEntity();
            CommentsEntity saved = commentsRepository.save(entity);
            return saved.toDto();
        } catch (Exception e) {
            log.error("댓글 등록 실패", e);
            return null;
        }
    }

    // 댓글 수정
    public int updateComment(Comments dto) {
        try {
            CommentsEntity entity = commentsRepository.findById(dto.getCommentId()).orElse(null);
            if (entity == null) {
                return 0;
            }

            entity.setContent(dto.getContent());
            entity.setUpdateAt(new Date(System.currentTimeMillis()));

            commentsRepository.save(entity);
            return 1;
        } catch (Exception e) {
            log.error("댓글 수정 실패", e);
            return 0;
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
}
