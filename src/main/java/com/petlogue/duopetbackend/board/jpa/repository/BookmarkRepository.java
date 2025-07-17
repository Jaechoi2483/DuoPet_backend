package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {

    // contentId를 사용하는 메서드
    Optional<BookmarkEntity> findByUserIdAndContentIdAndTargetType(Long userId, Long contentId, String targetType);

    // 북마크 취소 시 사용
    void deleteByUserIdAndContentIdAndTargetType(Long userId, Long contentId, String targetType);
}