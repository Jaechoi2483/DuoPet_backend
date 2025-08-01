package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.mypage.model.dto.MyCommentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<CommentsEntity, Long> {
    // 댓글 + 대댓글 정렬 순 조회

    List<CommentsEntity> findByContentIdAndStatusOrderByParentCommentIdAscCreatedAtAsc(Long contentId, String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM CommentsEntity c WHERE c.contentId = :contentId")
    void deleteAllByContentId(Long contentId);

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

    // 신고 관리용
    List<CommentsEntity> findAllByContentId(Long contentId);

    /**
     * 사용자가 작성한 댓글 목록을 게시글 제목과 함께 조회 - 마이페이지
     */
    @Query("SELECT new com.petlogue.duopetbackend.mypage.model.dto.MyCommentDto(" +
            "c.commentId, c.content, " +
            "TO_CHAR(c.createdAt, 'YYYY.MM.DD HH24:MI'), " +
            "c.likeCount, b.category, b.title, b.contentId) " +
            "FROM CommentsEntity c JOIN BoardEntity b ON c.contentId = b.contentId " +
            "WHERE c.user.userId = :userId AND c.status = 'ACTIVE' " +
            "ORDER BY c.createdAt DESC")
    List<MyCommentDto> findMyCommentsByUserId(@Param("userId") Long userId);

}