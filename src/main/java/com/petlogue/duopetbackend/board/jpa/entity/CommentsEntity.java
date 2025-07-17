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

    @Column(name = "USER_ID", nullable = false)
    private Long userId;     // 댓글 작성자 ID (FK)

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;  // 댓글 내용

    @Column(name = "PARENT_COMMENT_ID")
    private Long parentCommentId;  // 대댓글인 경우 상위 댓글 ID (nullable)

    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;  // 댓글 작성일

    @Column(name = "UPDATE_AT")
    private Date updateAt;   // 댓글 수정일 (nullable)

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
                .userId(userId)
                .content(content)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .updateAt(updateAt)
                .build();
    }
}
