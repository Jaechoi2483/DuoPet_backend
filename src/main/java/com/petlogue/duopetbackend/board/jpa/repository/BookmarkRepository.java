package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {

    // 사용자 + 게시글 + 유형으로 중복 여부 체크
    Optional<BookmarkEntity> findByUserIdAndContentIdAndTargetType(Long userId, Long contentId, String targetType);
    // 북마크 해제 시 사용
    void deleteByUserIdAndContentIdAndTargetType(Long userId, Long contentId, String targetType);
    // 마이페이지 > 내가 북마크한 게시글 전체 조회
    java.util.List<BookmarkEntity> findAllByUserId(Long userId);
    }
