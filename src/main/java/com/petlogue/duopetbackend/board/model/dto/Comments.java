package com.petlogue.duopetbackend.board.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
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
public class Comments {

    private Long commentId;         // 댓글 고유 식별자 (PK)
    private Long contentId;         // 게시글 ID (FK: BOARD.CONTENT_ID)
    private Long userId;            // 댓글 작성자 ID (FK: USERS.USER_ID)
    private String content;         // 댓글 본문
    private Long parentCommentId;   // 부모 댓글 ID (null이면 일반 댓글)
    private Date createdAt;         // 댓글 작성일시
    private Date updateAt;          // 댓글 수정일시
    private int likeCount;          // 댓글 좋아요 카운트
    private int reportCount;        // 댓글 신고하기 카운트
    private String nickname;        // 닉네임
    @Builder.Default
    private String status = "ACTIVE";


    public CommentsEntity toEntity(UserEntity user) {
        return CommentsEntity.builder()
                .commentId(commentId)
                .contentId(contentId)
                .content(content)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .updateAt(updateAt)
                .likeCount(likeCount)
                .reportCount(reportCount)
                .user(user)
                .status(this.status)
                .build();
    }
}
