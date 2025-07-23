package com.petlogue.duopetbackend.board.controller;

import com.petlogue.duopetbackend.board.model.dto.VideoInfo;
import com.petlogue.duopetbackend.board.model.dto.VideoRecommendRequest;
import com.petlogue.duopetbackend.board.model.service.VideoRecommendService;
import com.petlogue.duopetbackend.common.response.ApiResponse;
import com.petlogue.duopetbackend.common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/video-recommend")
public class VideoRecommendController {

    private final VideoRecommendService videoRecommendService;

    @PostMapping("/recommend")
    public ApiResponse<?> recommendVideos(@RequestBody VideoRecommendRequest request) {
        log.info("[추천요청] 게시글 ID: {}", request.getContentId());

        List<VideoInfo> result = videoRecommendService.recommendVideos(request);

        return ResponseUtil.success("추천 영상 리스트", result);
    }

    @GetMapping("/categories")
    public ApiResponse<?> getCategories() {
        return ResponseUtil.success("게시판 카테고리 목록", List.of(
                new Category("free", "자유"),
                new Category("review", "후기"),
                new Category("question", "질문"),
                new Category("tip", "팁")
        ));
    }

    // 내부 클래스로 카테고리 정의 (필요 시 별도 DTO로 분리해도 됨)
    public record Category(String id, String name) {}
}

