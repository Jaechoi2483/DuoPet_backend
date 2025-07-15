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

    private static final String TARGET_TYPE = "자유"; // 기본값. 필요 시 파라미터로 확장 가능

    @Transactional
    public Bookmark toggleBookmark(Long userId, Long contentId) {

        Optional<BookmarkEntity> existing = bookmarkRepository
                .findByUserIdAndContentIdAndTargetType(userId, contentId, TARGET_TYPE);

        boolean bookmarked;

        if (existing.isPresent()) {
            // 북마크 해제
            bookmarkRepository.delete(existing.get());
            bookmarked = false;
        } else {
            // 북마크 등록
            BookmarkEntity bookmark = BookmarkEntity.builder()
                    .userId(userId)
                    .contentId(contentId)
                    .targetType(TARGET_TYPE)
                    .createdAt(new Date())
                    .build();

            bookmarkRepository.save(bookmark);
            bookmarked = true;
        }

        return Bookmark.builder()
                .userId(userId)
                .contentId(contentId)
                .targetType(TARGET_TYPE)
                .createdAt(new Date())
                .bookmarked(bookmarked)
                .build();
    }
}
