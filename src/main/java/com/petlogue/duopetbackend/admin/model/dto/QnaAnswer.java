package com.petlogue.duopetbackend.admin.model.dto;


import com.petlogue.duopetbackend.admin.jpa.entity.QnaAnswerEntity;
import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
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
    private Integer parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QnaAnswerEntity toEntity(QnaEntity parentQna) {
        return QnaAnswerEntity.builder()
                .commentId(commentId)
                .userId(userId)
                .content(content)
                .parentCommentId(parentCommentId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .qna(parentQna)
                .build();
    }
}
