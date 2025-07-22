package com.petlogue.duopetbackend.board.controller;

import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
import com.petlogue.duopetbackend.board.jpa.repository.LikeRepository;
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

    private final CommentsRepository commentsRepository;

    private final ReportService reportService;

    private final LikeService likeService;

    private final LikeRepository likeRepository;

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

        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromRequest(request);

        dto.setUserId(userId);

        if (commentsService.insertComment(dto) != null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long userId) {

        CommentsEntity comment = commentsRepository.findById(commentId)
                .orElse(null);

        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("존재하지 않는 댓글입니다.");
        }

        // 댓글 작성자와 현재 로그인한 사용자 비교
        if (!comment.getUser().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("작성자만 댓글을 삭제할 수 있습니다.");
        }

        // 삭제 진행
        commentsRepository.deleteById(commentId);
        return ResponseEntity.ok().body("댓글 삭제 완료");
    }

    // 댓글 좋아요
    @PostMapping("/comment-like/{commentId}")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId,
                                         @RequestAttribute("userId") Long userId) {

        int likeCount = likeService.toggleCommentLike(userId, commentId);
        return ResponseEntity.ok().body(likeCount); // 최신 좋아요 수 반환
    }

    // 댓글 신고
    @PostMapping("/comment-report/{commentId}")
    public ResponseEntity<?> reportComment(@PathVariable Long commentId,
                                           @RequestAttribute("userId") Long userId,
                                           @RequestBody Report dto) {
        dto.setTargetId(commentId);
        dto.setTargetType("comment");
        dto.setUserId(userId);

        reportService.saveReport(userId, dto);
        return ResponseEntity.ok("신고 접수 완료");
    }
}