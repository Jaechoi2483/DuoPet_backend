package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.model.dto.Board;
import com.petlogue.duopetbackend.common.FileNameChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

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
            return null;
        }
    }

    // 게시글 수정
    public void updateBoard(Long id, Board updatedDto, MultipartFile file) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        board.setTitle(updatedDto.getTitle());
        board.setContentBody(updatedDto.getContentBody());
        board.setCategory(updatedDto.getCategory());
        board.setTags(updatedDto.getTags());

        if (file != null && !file.isEmpty()) {
            String savePath = uploadDir + "/board";
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String renamedName = FileNameChange.change(originalName, "yyyyMMddHHmmss");

            try {
                file.transferTo(new File(savePath, renamedName));
                board.setOriginalFilename(originalName);
                board.setRenameFilename(renamedName);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패", e);
            }
        }

        boardRepository.save(board);
    }

    // 게시글 삭제
    public void deleteBoard(Long id) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 게시글이 존재하지 않습니다."));

        if (board.getRenameFilename() != null) {
            File file = new File(uploadDir + "/board/" + board.getRenameFilename());
            if (file.exists()) {
                file.delete();
            }
        }

        boardRepository.delete(board);
    }

    // 좋아요 TOP3
    public List<Board> getTopLikedBoards() {
        List<Object[]> results = boardRepository.findTop3Liked(PageRequest.of(0, 3));

        List<Board> list = new ArrayList<>();
        for (Object[] row : results) {
            Board board = new Board();
            board.setContentId(((Number) row[0]).longValue());
            board.setTitle((String) row[1]);
            board.setUserId(((Number) row[2]).longValue());
            board.setLikeCount(((Number) row[3]).intValue());
            list.add(board);
        }
        return list;
    }

    // 조회수 TOP3
    public List<Board> getTopViewedBoards() {
        List<Object[]> results = boardRepository.findTop3Viewed(PageRequest.of(0, 3));

        List<Board> list = new ArrayList<>();
        for (Object[] row : results) {
            Board board = new Board();
            board.setContentId(((Number) row[0]).longValue());
            board.setTitle((String) row[1]);
            board.setUserId(((Number) row[2]).longValue());
            board.setViewCount(((Number) row[3]).intValue());
            list.add(board);
        }
        return list;
    }

    // 게시글 수 조회
    public int selectListCount() {
        return (int) boardRepository.count();
    }

    // 페이징 리스트
    public ArrayList<Board> selectList(Pageable pageable) {
        Page<BoardEntity> page = boardRepository.findByCategory("자유", pageable);
        ArrayList<Board> list = new ArrayList<>();
        for (BoardEntity entity : page) {
            list.add(entity.toDto());
        }
        return list;
    }
}
