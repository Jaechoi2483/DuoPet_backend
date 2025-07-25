package com.petlogue.duopetbackend.admin.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petlogue.duopetbackend.admin.model.dto.QnaAnswer;
import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    private int commentId;

    @Column(name = "USER_ID", nullable = false)
    private int userId;
    @Column(name = "CONTENT", nullable = false)
    private  String content;
    @Column(name = "PARENT_COMMENT_ID",  length = 1000)
 
    private Integer parentCommentId;

    

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "UPDATE_AT")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTENT_ID", nullable = false)
    @JsonIgnore
    private QnaEntity qna;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public QnaAnswer toDto() {
        return QnaAnswer.builder()
                .commentId(commentId)
                .contentId(qna.getContentId())
                .userId(userId)
                .content(content)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)

                .build();
    }
}
