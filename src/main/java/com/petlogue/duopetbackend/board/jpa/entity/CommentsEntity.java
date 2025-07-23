package com.petlogue.duopetbackend.board.jpa.entity;

import com.petlogue.duopetbackend.board.model.dto.Comments;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "COMMENTS")
public class CommentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID", nullable = false)
    private Long commentId;  // 댓글 고유 식별자 (PK)

    @Column(name = "CONTENT_ID", nullable = false)
    private Long contentId;  // 댓글이 달린 게시글 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;    // 유저 연관관계 추가 닉네임, 댓글 작성자 ID (FK)

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;  // 댓글 내용

    @Column(name = "PARENT_COMMENT_ID")
    private Long parentCommentId;  // 대댓글인 경우 상위 댓글 ID (nullable)

    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;  // 댓글 작성일

    @Column(name = "UPDATE_AT")
    private Date updateAt;   // 댓글 수정일 (nullable)

    @Column(name = "LIKE_COUNT", nullable = false)
    private int likeCount;  // 댓글 좋아요 카운트

    @Column(name = "REPORT_COUNT", nullable = false)
    private int reportCount;    // 댓글 신고하기 카운트

    @Column(name = "status", nullable = false)
    @Builder.Default // 빌더 사용 시 기본값 설정
    private String status = "ACTIVE";

    /**
     * INSERT 시 자동으로 현재 시간으로 createdAt 값 설정
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = new Date(System.currentTimeMillis());
    }


    public Comments toDto() {
        return Comments.builder()
                .commentId(commentId)
                .contentId(contentId)
                .content(content)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .updateAt(updateAt)
                .likeCount(likeCount)
                .reportCount(reportCount)
                .userId(user != null ? user.getUserId() : null)
                .nickname(user != null ? user.getNickname() : "알 수 없음")
                .status(status)
                .build();
    }
}
