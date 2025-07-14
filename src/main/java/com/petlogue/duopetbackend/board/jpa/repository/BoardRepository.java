package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
    int countByTitleContainingOrContentBodyContaining(String title, String content);

    // 게시글 목록
    Page<BoardEntity> findByCategoryAndTitleContainingOrContentBodyContaining(
            String category, String title, String content, Pageable pageable);
}


