package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.model.dto.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    // 전체 게시물 목록
    public List<Board> getAllFreeBoards() {
        List<BoardEntity> entities = boardRepository.findByContentTypeAndCategory("board", "자유");

        List<Board> boardList = new ArrayList<>();
        for (BoardEntity entity : entities) {
            boardList.add(entity.toDto());
        }

        return boardList;
    }

    // 게시물 상세보기
    public Board getBoardDetail(Number id) {
        Optional<BoardEntity> optionalBoard = boardRepository.findById(id);

        if (optionalBoard.isPresent()) {
            BoardEntity entity = optionalBoard.get();
            return entity.toDto();
        } else {
            return null; // 또는 throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }
    }

    // 좋아요 TOP3
    public List<Board> getTopLikedBoards() {
        List<Object[]> results = boardRepository.findTop3Liked(PageRequest.of(0, 3)); // 커스텀 쿼리 사용

        List<Board> list = new ArrayList<>();
        for (Object[] row : results) {
            Board board = new Board();
            board.setContentId(((Number) row[0]).longValue());   // 게시글 ID
            board.setTitle((String) row[1]);                         // 제목
            board.setUserId(((Number) row[2]).longValue());      // 작성자 ID
            board.setLikeCount(((Number) row[3]).intValue());    // 좋아요 수
            list.add(board);
        }
        return list;
    }

    // 조회수 TOP3
    public List<Board> getTopViewedBoards() {
        List<Object[]> results = boardRepository.findTop3Viewed(PageRequest.of(0, 3)); // 커스텀 쿼리 사용

        List<Board> list = new ArrayList<>();
        for (Object[] row : results) {
            Board board = new Board();
            board.setContentId(((Number) row[0]).longValue());   // 게시글 ID
            board.setTitle((String) row[1]);                          // 제목
            board.setUserId(((Number) row[2]).longValue());      // 작성자 ID
            board.setViewCount(((Number) row[3]).intValue());    // 조회수
            list.add(board);
        }
        return list;
    }


    // 게시글 수 조회
    public int selectListCount() {
        return (int) boardRepository.count();
    }

    // 페이징처리 Service에서 DTO 변환 처리
    public ArrayList<Board> selectList(Pageable pageable) {
        Page<BoardEntity> page = boardRepository.findByCategory("자유", pageable);
        ArrayList<Board> list = new ArrayList<>();
        for(BoardEntity entity : page){
            list.add(entity.toDto());
        }
        return list;
    }
}
