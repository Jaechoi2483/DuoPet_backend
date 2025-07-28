package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findByContentTypeAndCategoryAndStatus(String contentType, String category, String status);

    // 좋아요 상위 3개 (게시판 구분 가능하도록)
    @Query("SELECT b.contentId, b.title, b.userId, b.likeCount, u.nickname, b.createdAt, b.viewCount " +
            "FROM BoardEntity b JOIN UserEntity u ON b.userId = u.userId " +
            "WHERE b.category = :category AND b.contentType = :contentType AND b.status = 'ACTIVE' " +
            "ORDER BY b.likeCount DESC")
    List<Object[]> findTop3LikedByCategory(@Param("category") String category,
                                           @Param("contentType") String contentType,
                                           Pageable pageable);

    // 조회수 상위 3개
    @Query("SELECT b.contentId, b.title, b.userId, b.viewCount, u.nickname, b.createdAt, b.likeCount " +
            "FROM BoardEntity b JOIN UserEntity u ON b.userId = u.userId " +
            "WHERE b.category = :category AND b.contentType = :contentType AND b.status = 'ACTIVE' " +
            "ORDER BY b.viewCount DESC")
    List<Object[]> findTop3ViewedByCategory(@Param("category") String category,
                                            @Param("contentType") String contentType,
                                            Pageable pageable);

    // 메인에서 좋아요 TOP 3
    @Query("SELECT b.contentId, b.title, b.userId, b.likeCount, u.nickname, b.createdAt, b.viewCount, b.category " +
            "FROM BoardEntity b JOIN UserEntity u ON b.userId = u.userId " +
            "WHERE b.contentType = 'board' AND b.status = 'ACTIVE' AND b.category IN :categoryList " +
            "ORDER BY b.likeCount DESC")
    List<Object[]> findTop3LikedForMain(@Param("categoryList") List<String> categoryList, Pageable pageable);

    // 메인에서 조회수 TOP3
    @Query("SELECT b.contentId, b.title, b.userId, b.viewCount, u.nickname, b.createdAt, b.likeCount, b.category " +
            "FROM BoardEntity b JOIN UserEntity u ON b.userId = u.userId " +
            "WHERE b.contentType = 'board' AND b.status = 'ACTIVE' AND b.category IN :categoryList " +
            "ORDER BY b.viewCount DESC")
    List<Object[]> findTop3ViewedForMain(@Param("categoryList") List<String> categoryList, Pageable pageable);

    Page<BoardEntity> findByCategoryAndStatus(String category, String status, Pageable pageable);

    // 게시글 수
    // 전체 게시글 수
    int countByCategoryAndContentTypeAndStatus(String category, String contentType, String status);
    // 제목 검색 조건 포함 (게시판 + 키워드)
    int countByCategoryAndContentTypeAndTitleContainingAndStatus(String category, String contentType, String title, String status);
    // 날짜 검색 조건 포함 (게시판 + 날짜)
    int countByCategoryAndContentTypeAndCreatedAtBetweenAndStatus(String category, String contentType, Date start, Date end, String status);

    // 게시글 목록
    // 게시글 목록 조회
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