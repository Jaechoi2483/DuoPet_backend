package com.petlogue.duopetbackend.admin.model.dto;


import com.petlogue.duopetbackend.admin.jpa.entity.QnaAnswerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnaAnswer {
    private int commentId;
    private  int contentId;
    private int userId;
    private  String content;
    private int parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QnaAnswerEntity toEntity() {
        return QnaAnswerEntity.builder()
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
