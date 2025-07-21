package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<CommentsEntity, Long> {
    // 댓글 + 대댓글 정렬 순 조회
    List<CommentsEntity> findByContentIdOrderByParentCommentIdAscCreatedAtAsc(Long contentId);

    // 신고관리 에서 게시글 삭제 처리할때 댓글도 삭제시키는 거에요
    @Modifying
    @Transactional
    void deleteAllByContentId(Long contentId);
}