package com.petlogue.duopetbackend.board.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoInfo {

    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String channelName;
    private String publishedAt;
}
