package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {

    // contentId를 사용하는 메서드
    Optional<BookmarkEntity> findByUserIdAndContentIdAndTargetType(Long userId, Long contentId, String targetType);

    // 북마크 취소 시 사용
    void deleteByUserIdAndContentIdAndTargetType(Long userId, Long contentId, String targetType);

    // 특정 게시물의 연결된 북마크 데이터 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM BookmarkEntity b WHERE b.contentId = :contentId")
    void deleteAllByContentId(@Param("contentId") Long contentId);

    /**
     * Bookmark 테이블과 연결되는 JPA Repository - 마이페이지
     */
    List<BookmarkEntity> findAllByUserId(Long userId);

    /**
     * 특정 사용자와 대상 ID, 타입으로 북마크가 존재하는지 확인
     */
    boolean existsByUserIdAndContentIdAndTargetType(Long userId, Long contentId, String targetType);
}