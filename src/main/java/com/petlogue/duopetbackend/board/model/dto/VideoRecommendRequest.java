package com.petlogue.duopetbackend.board.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoRecommendRequest {

    private Long contentId;
    private String category;
    private int maxResults = 5;
}
