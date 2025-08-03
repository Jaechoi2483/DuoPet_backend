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

    // 게시글 기반 추천 영상 요청
    @PostMapping("/recommend")
    public ApiResponse<?> recommendVideos(@RequestBody VideoRecommendRequest request) {
        log.info("[추천요청] 게시글 ID: {}", request.getContentId());

        // VideoRecommendService에서 FastAPI 서버와 통신 → 추천 영상 List 반환
        List<VideoInfo> result = videoRecommendService.recommendVideos(request);

        // 공통 응답 포맷으로 감싸서 반환
        return ResponseUtil.success("추천 영상 리스트", result);
    }

    // 게시판 카테고리 목록 조회
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

