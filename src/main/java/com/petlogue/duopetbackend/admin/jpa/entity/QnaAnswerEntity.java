package com.petlogue.duopetbackend.admin.jpa.entity;

import com.petlogue.duopetbackend.admin.model.dto.QnaAnswer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "COMMENTS")
@Entity
public class QnaAnswerEntity {
    @Id
    @Column(name = "COMMENT_ID")
    private int commentId;
    @Column(name = "CONTENT_ID", nullable = false)
    private  int contentId;
    @Column(name = "USER_ID", nullable = false)
    private int userId;
    @Column(name = "CONTENT", nullable = false)
    private  String content;
    @Column(name = "PARENT_COMMENT_ID",  length = 1000)
    private int parentCommentId;
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "UPDATE_AT")
    private LocalDateTime updatedAt;

    public QnaAnswer toDto() {
        return QnaAnswer.builder()
                .commentId(commentId)
                .contentId(contentId)
                .userId(userId)
                .content(content)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
