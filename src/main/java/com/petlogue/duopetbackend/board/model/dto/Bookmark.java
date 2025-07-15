package com.petlogue.duopetbackend.board.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.BookmarkEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bookmark {

    private Long bookmarkId;       // 북마크 고유 식별자 (PK)
    private Long userId;           // 북마크를 한 사용자 ID
    private Long contentId;        // 북마크 대상 게시글 ID
    private String targetType;     // 게시글 유형 (자유, 후기, 팁 등)
    private Date createdAt;        // 북마크 생성일시

    private Boolean bookmarked;  // 현재 북마크 상태

    // DTO → Entity 변환 메서드
    public BookmarkEntity toEntity() {
        return BookmarkEntity.builder()
                .bookmarkId(bookmarkId)         // ID 설정 (수정/조회용)
                .userId(userId)                 // 사용자 ID
                .contentId(contentId)           // 게시글 ID
                .targetType(targetType)         // 유형
                .createdAt(createdAt)           // 생성일자
                .build();
    }
}
