package com.petlogue.duopetbackend.board.controller;

import com.petlogue.duopetbackend.board.model.dto.Comments;
import com.petlogue.duopetbackend.board.model.dto.Report;
import com.petlogue.duopetbackend.board.model.service.CommentsService;
import com.petlogue.duopetbackend.board.model.service.LikeService;
import com.petlogue.duopetbackend.board.model.service.ReportService;
import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/comments")
@CrossOrigin
public class CommentsController {

    private final CommentsService commentsService;

    private final ReportService reportService;

    private final LikeService likeService;

    private final JWTUtil jwtUtil;

    // 댓글 + 대댓글 조회
    @GetMapping("/view/{contentId}")
    public ResponseEntity<ArrayList<Comments>> selectCommentList(@PathVariable Long contentId) {
        ArrayList<Comments> list = commentsService.selectCommentList(contentId);
        return ResponseEntity.ok(list);
    }

    // 댓글 등록
    @PostMapping("/insert")
    public ResponseEntity<?> insertComment(@RequestBody Comments dto, HttpServletRequest request) {
        log.info("insertComment: {}", dto);

        // JWT 토큰에서 사용자 ID 추출
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromRequest(request);

        dto.setUserId(userId);

        // 댓글 등록 시도
        if (commentsService.insertComment(dto) != null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 댓글 좋아요
    @PostMapping("/comment-like/{commentId}")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId,
                                         @RequestAttribute("userId") Long userId) {

        // 좋아요 처리 후 현재 좋아요 수 반환
        int likeCount = likeService.toggleCommentLike(userId, commentId);
        return ResponseEntity.ok().body(likeCount); // 최신 좋아요 수 반환
    }

    // 댓글 신고
    @PostMapping("/comment-report/{commentId}")
    public ResponseEntity<?> reportComment(@PathVariable Long commentId,
                                           @RequestAttribute("userId") Long userId,
                                           @RequestBody Report dto) {

        // 신고 정보 설정
        dto.setTargetId(commentId);
        dto.setTargetType("comment");
        dto.setUserId(userId);

        // 신고 저장 처리
        reportService.saveReport(userId, dto);
        return ResponseEntity.ok("신고 접수 완료");
    }
}