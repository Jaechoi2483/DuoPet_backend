package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findByContentTypeAndCategoryAndStatus(String contentType, String category, String status);

    @Query("SELECT b.contentId, b.title, b.userId, b.likeCount " +
            "FROM BoardEntity b " +
            "WHERE b.category = '자유' AND b.contentType = 'board' AND b.status = 'ACTIVE' " +
            "ORDER BY b.likeCount DESC")
    List<Object[]> findTop3Liked(Pageable pageable);

    @Query("SELECT b.contentId, b.title, b.userId, b.viewCount " +
            "FROM BoardEntity b " +
            "WHERE b.category = '자유' AND b.contentType = 'board' AND b.status = 'ACTIVE' " +
            "ORDER BY b.viewCount DESC")
    List<Object[]> findTop3Viewed(Pageable pageable);

    Page<BoardEntity> findByCategoryAndStatus(String category, String status, Pageable pageable);

    // 게시글 수
    // 자유게시판 글 수
    int countByCategoryAndContentTypeAndStatus(String category, String contentType, String status);
    // 자유게시판 + 키워드
    int countByCategoryAndContentTypeAndTitleContainingAndStatus(String category, String contentType, String title, String status);
    // 자유게시판 + 날짜
    int countByCategoryAndContentTypeAndCreatedAtBetweenAndStatus(String category, String contentType, Date start, Date end, String status);

    // 게시글 목록
    // 자유게시판 목록
    Page<BoardEntity> findByCategoryAndContentTypeAndStatus(String category, String contentType, String status, Pageable pageable);
    // 키워드만 있을 때
    Page<BoardEntity> findByCategoryAndContentTypeAndTitleContainingAndStatus(String category, String contentType, String keyword, String status, Pageable pageable);
    // 날짜만 있을 때
    Page<BoardEntity> findByCategoryAndContentTypeAndCreatedAtBetweenAndStatus(String category, String contentType, Date start, Date end, String status, Pageable pageable);

    Optional<BoardEntity> findByContentIdAndStatus(Long contentId, String status);

    /**
     * 특정 사용자가 작성한 게시글을 최신순으로 조회 - 마이페이지
     */
    List<BoardEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}