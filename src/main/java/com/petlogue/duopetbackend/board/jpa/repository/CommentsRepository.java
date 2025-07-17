package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<CommentsEntity, Long> {
    // 댓글 + 대댓글 정렬 순 조회
    List<CommentsEntity> findByContentIdOrderByParentCommentIdAscCreatedAtAsc(Long contentId);
}