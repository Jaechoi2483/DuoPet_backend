package com.petlogue.duopetbackend.mypage.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyLikeDto {

    private Long contentId;
    private String title;
    private String content;
    private String category;
    private String createdAt;
    private int likeCount;
    private int viewCount;

    public static MyLikeDto from(BoardEntity board) {
        return MyLikeDto.builder()
                .contentId(board.getContentId())
                .title(board.getTitle())
                .content(board.getContentBody())
                .category(board.getCategory()) // 자유게시판 / 후기게시판 등
                .createdAt(board.getCreatedAt().toString()) // 포맷은 프론트에서 하도록 기본 문자열로 전달
                .likeCount(board.getLikeCount())
                .viewCount(board.getViewCount())
                .build();
    }
}