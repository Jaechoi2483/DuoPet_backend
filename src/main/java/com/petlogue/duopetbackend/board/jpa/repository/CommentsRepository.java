package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.model.dto.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<CommentsEntity, Long> {
    // 댓글 + 대댓글 정렬 순 조회
    List<CommentsEntity> findByContentIdOrderByParentCommentIdAscCreatedAtAsc(Long contentId);

    // 좋아요 수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE CommentsEntity c SET c.likeCount = c.likeCount + 1 WHERE c.commentId = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    // 좋아요 수 감소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE CommentsEntity c SET c.likeCount = c.likeCount - 1 WHERE c.commentId = :commentId")
    void decrementLikeCount(@Param("commentId") Long commentId);

    // 댓글 ID 기준 좋아요 수 조회
    @Query("SELECT c.likeCount FROM CommentsEntity c WHERE c.commentId = :commentId")
    int findLikeCountByCommentId(@Param("commentId") Long commentId);

    // 신고 수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE CommentsEntity c SET c.reportCount = c.reportCount + 1 WHERE c.commentId = :commentId")
    void incrementReportCount(@Param("commentId") Long commentId);
}