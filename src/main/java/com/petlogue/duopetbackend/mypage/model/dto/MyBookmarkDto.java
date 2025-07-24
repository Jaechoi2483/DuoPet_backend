package com.petlogue.duopetbackend.mypage.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyBookmarkDto {

    private Long contentId;
    private String title;
    private String content;
    private String category;
    private String createdAt;
    private int likeCount;
    private int viewCount;

    public static MyBookmarkDto from(BoardEntity board) {
        return MyBookmarkDto.builder()
                .contentId(board.getContentId())
                .title(board.getTitle())
                .content(board.getContentBody())
                .category(board.getCategory())
                .createdAt(board.getCreatedAt().toString())
                .likeCount(board.getLikeCount())
                .viewCount(board.getViewCount())
                .build();
    }
}