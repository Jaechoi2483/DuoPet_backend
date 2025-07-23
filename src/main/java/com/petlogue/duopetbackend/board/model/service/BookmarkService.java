package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.BookmarkEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.BookmarkRepository;
import com.petlogue.duopetbackend.board.model.dto.Bookmark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final BoardRepository boardRepository;

    public Bookmark toggleBookmark(Long userId, Long boardId) {
        String targetType = "board";  // targetType은 "board"로 고정

        // `contentId`를 사용하는 메서드 호출
        Optional<BookmarkEntity> existing = bookmarkRepository.findByUserIdAndContentIdAndTargetType(userId, boardId, targetType);

        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (existing.isPresent()) {
            // 북마크가 이미 있으면 삭제
            bookmarkRepository.deleteByUserIdAndContentIdAndTargetType(userId, boardId, targetType);
            board.setBookmarkCount(Math.max(board.getBookmarkCount() - 1, 0));  // 음수 방지
            boardRepository.save(board);
            log.info("북마크 취소 완료 - contentId: {}, userId: {}", boardId, userId);
            return new Bookmark(boardId, false);  // 북마크가 취소되었음을 반환
        } else {
            // 북마크가 없으면 추가
            BookmarkEntity newBookmark = BookmarkEntity.builder()
                    .userId(userId)
                    .contentId(boardId)  // contentId로 boardId 사용
                    .targetType(targetType)  // targetType은 "board"
                    .build();
            bookmarkRepository.save(newBookmark);
            int currentCount = Optional.ofNullable(board.getBookmarkCount()).orElse(0);
            board.setBookmarkCount(currentCount  + 1);  // 북마크 수 증가
            boardRepository.save(board);
            log.info("북마크 등록 완료 - contentId: {}, userId: {}", boardId, userId);
            return new Bookmark(boardId, true);  // 북마크가 등록되었음을 반환
        }
    }

    /**
     * 특정 게시글에 대해 사용자가 북마크했는지 여부 반환
     */
    public boolean isBookmarked(Long userId, Long contentId) {
        return bookmarkRepository.existsByUserIdAndContentIdAndTargetType(userId, contentId, "board");
    }
}
