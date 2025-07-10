package com.petlogue.duopetbackend.board.controller;

import com.petlogue.duopetbackend.board.model.dto.Board;
import com.petlogue.duopetbackend.board.model.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/board")
@CrossOrigin
public class BoardController {

    private final BoardService boardService;

    // 전체 게시판 목록 조회
    @GetMapping("/free")
    public List<Board> getFreeBoardList() {
        log.info("자유게시판 전체 조회 요청");
        return boardService.getAllFreeBoards();
    }

    // 게시글 상세 조회
    @GetMapping("/detail/{id}")
    public Board getBoardDetail(@PathVariable Number id) {
        log.info("게시글 상세 조회 요청: {}", id);
        Board board = boardService.getBoardDetail(id);
        if (board == null) {
            // 예외 대신 null 반환을 선택한 경우 프론트에서 처리하게끔 함
            log.warn("해당 게시글을 찾을 수 없습니다. id: {}", id);
            return null; // 또는 ResponseEntity로 감싸도 됨
        }

        return board;
    }

    // 좋아요 TOP3
    @GetMapping("/top-liked")
    public List<Board> getTopLikedBoards() {
        log.info("TOP 좋아요 게시글 요청");
        return boardService.getTopLikedBoards();
    }

    // 조회수 TOP3
    @GetMapping("/top-viewed")
    public List<Board> getTopViewedBoards() {
        log.info("TOP 조회수 게시글 요청");
        return boardService.getTopViewedBoards();
    }
}
