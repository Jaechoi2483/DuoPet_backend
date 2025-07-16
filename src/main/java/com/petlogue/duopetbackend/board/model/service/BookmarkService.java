package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BookmarkEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BookmarkRepository;
import com.petlogue.duopetbackend.board.model.dto.Bookmark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    private static final String TARGET_TYPE = "board"; // 게시판 타입 (DB와 통일해야 함)

    public Bookmark toggleBookmark(Long userId, Long contentId) {

        Optional<BookmarkEntity> existing = bookmarkRepository
                .findByUserIdAndContentIdAndTargetType(userId, contentId, TARGET_TYPE);

        BookmarkEntity bookmarkEntity;

        boolean bookmarked;

        if (existing.isPresent()) {
            // 북마크 해제
            bookmarkRepository.delete(existing.get());
            bookmarked = false;
            log.info("북마크 해제 - userId: {}, contentId: {}", userId, contentId);
            bookmarkEntity = existing.get(); // 삭제 전 상태 보존용
        } else {
            // 북마크 등록
            bookmarkEntity = BookmarkEntity.builder()
                    .userId(userId)
                    .contentId(contentId)
                    .targetType(TARGET_TYPE)
                    .build();

            bookmarkRepository.save(bookmarkEntity);
            bookmarked = true;
            log.info("북마크 등록 - userId: {}, contentId: {}", userId, contentId);
        }

        return Bookmark.builder()
                .userId(userId)
                .contentId(contentId)
                .targetType(TARGET_TYPE)
                .createdAt(bookmarkEntity.getCreatedAt()) // 등록된 날짜 사용
                .bookmarked(bookmarked)
                .build();
    }
}
