package com.petlogue.duopetbackend.notice.model.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.petlogue.duopetbackend.notice.jpa.entity.NoticeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notice {

    private int contentId;
    private int viewCount;
    private int likeCount;
    private String renameFilename;
    private String originalFilename;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime  createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private int userId;
    private String title;
    private String contentBody;
    private String contentType;
    private String category;
    private String tags;


    public NoticeEntity toEntity() {
        return NoticeEntity.builder()
                .contentId(contentId)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .renameFilename(renameFilename)
                .originalFilename(originalFilename)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .userId(userId)
                .title(title)
                .contentBody(contentBody)
                .contentType(contentType)
                .category(category)
                .tags(tags)
                .build();
    }


}
