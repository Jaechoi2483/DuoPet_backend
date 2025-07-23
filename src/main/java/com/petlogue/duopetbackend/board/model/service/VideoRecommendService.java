package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.model.dto.VideoRecommendRequest;
import com.petlogue.duopetbackend.board.model.dto.VideoInfo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoRecommendService {

    private final BoardRepository boardRepository;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8000/api/v1/video-recommend") // ✅ FastAPI 서버 주소
            .build();

    public List<VideoInfo> recommendVideos(VideoRecommendRequest request) {
        try {
            // ✅ 1. 게시글 ID로 태그 조회
            BoardEntity board = boardRepository.findById(request.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("❌ 게시글을 찾을 수 없습니다. ID: " + request.getContentId()));

            String tagString = board.getTags();
            if (tagString == null || tagString.trim().isEmpty()) {
                log.warn("⚠️ 태그가 비어있습니다. 추천 요청 중단");
                return List.of();
            }

            List<String> keywords = Arrays.stream(tagString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .toList();

            log.info("🎯 추천 키워드 추출: {}", keywords);

            // ✅ 2. FastAPI로 요청
            JsonNode response = webClient.post()
                    .uri("/recommend")
                    .bodyValue(Map.of(
                            "keywords", keywords,
                            "category", request.getCategory(),
                            "max_results", request.getMaxResults()
                    ))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            // ✅ 3. 응답 파싱
            List<VideoInfo> videos = new ArrayList<>();
            JsonNode dataNode = response.get("data");
            if (dataNode != null && dataNode.has("videos")) {
                for (JsonNode video : dataNode.get("videos")) {
                    videos.add(VideoInfo.builder()
                            .videoId(video.get("video_id").asText())
                            .title(video.get("title").asText())
                            .description(video.get("description").asText())
                            .thumbnailUrl(video.get("thumbnail_url").asText())
                            .channelName(video.get("channel_name").asText())
                            .publishedAt(video.get("published_at").asText())
                            .build());
                }
            }

            log.info("✅ 추천 영상 {}개 수신 완료", videos.size());
            return videos;

        } catch (Exception e) {
            log.error("🔥 영상 추천 실패: {}", e.getMessage(), e);
            return List.of(); // 실패 시 빈 리스트 반환
        }
    }
}
