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
}
