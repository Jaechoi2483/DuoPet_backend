package com.petlogue.duopetbackend.mypage.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPostDto {
    private Long id;
    private String boardType;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private int viewCount;
    private int likeCount;

    public static MyPostDto from(BoardEntity entity) {
        return MyPostDto.builder()
                .id(entity.getContentId())
                .boardType(entity.getCategory())
                .title(entity.getTitle())
                .content(entity.getContentBody())
                .createdAt(convertDate(entity.getCreatedAt()))
                .viewCount(entity.getViewCount() != null ? entity.getViewCount() : 0)
                .likeCount(entity.getLikeCount() != null ? entity.getLikeCount() : 0)
                .build();
    }

    private static LocalDateTime convertDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}