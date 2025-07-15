package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Number> {
    List<BoardEntity> findByContentTypeAndCategory(String contentType, String category);

    @Query("SELECT b.contentId, b.title, b.userId, b.likeCount " +
            "FROM BoardEntity b " +
            "WHERE b.category = '자유' AND b.contentType = 'board' " +
            "ORDER BY b.likeCount DESC")
    List<Object[]> findTop3Liked(Pageable pageable);

    @Query("SELECT b.contentId, b.title, b.userId, b.viewCount " +
            "FROM BoardEntity b " +
            "WHERE b.category = '자유' AND b.contentType = 'board' " +
            "ORDER BY b.viewCount DESC")
    List<Object[]> findTop3Viewed(Pageable pageable);

    Page<BoardEntity> findByCategory(String category, Pageable pageable);

    // 게시글 수
    // 자유게시판 글 수
    int countByCategoryAndContentType(String category, String contentType);
    // 자유게시판 + 키워드
    int countByCategoryAndContentTypeAndTitleContaining(String category, String contentType, String title);
    // 자유게시판 + 날짜
    int countByCategoryAndContentTypeAndCreatedAtBetween(String category, String contentType, Date start, Date end);

    // 게시글 목록
    // 자유게시판 목록
    Page<BoardEntity> findByCategoryAndContentType(String category, String contentType, Pageable pageable);
    // 키워드만 있을 때
    Page<BoardEntity> findByCategoryAndContentTypeAndTitleContaining(String category, String contentType, String keyword, Pageable pageable);
    // 날짜만 있을 때
    Page<BoardEntity> findByCategoryAndContentTypeAndCreatedAtBetween(String category, String contentType, Date start, Date end, Pageable pageable);
}


