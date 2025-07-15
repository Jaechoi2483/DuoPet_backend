package com.petlogue.duopetbackend.board.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
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
public class Like {

    private Long likeId;         // 좋아요 고유 식별자
    private Long userId;         // 좋아요 누른 사용자 ID
    private Long targetId;       // 대상 ID (게시글 or 댓글)
    private String targetType;   // 대상 유형 (board, comment 등)
    private Date createdAt;      // 좋아요 생성일자

    private Boolean liked;       // 좋아요 상태

    public LikeEntity toLikeEntity() {
        return LikeEntity.builder()
                .likeId(likeId)
                .userId(userId)
                .targetId(targetId)
                .targetType(targetType)
                .createdAt(createdAt)
                .build();
    }
}
