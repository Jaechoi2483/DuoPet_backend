package com.petlogue.duopetbackend.mypage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyCommentDto {
    private Long commentId;
    private String content;
    private String createdAtStr;  // 문자열로 포맷된 날짜
    private int likeCount;
    private String boardType;     // 게시판 종류
    private String postTitle;     // 게시글 제목S
    private Long postId;
}