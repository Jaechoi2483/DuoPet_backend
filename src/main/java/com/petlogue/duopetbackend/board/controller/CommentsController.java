package com.petlogue.duopetbackend.board.controller;

import com.petlogue.duopetbackend.board.model.dto.Comments;
import com.petlogue.duopetbackend.board.model.service.CommentsService;
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

    // 댓글 수정
    @PutMapping("/update/{commentId}")
    public ResponseEntity<?> updateComment(@RequestBody Comments dto) {
        log.info("updateComment: {}", dto);
        if (commentsService.updateComment(dto) > 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        if (commentsService.deleteComment(commentId) > 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}