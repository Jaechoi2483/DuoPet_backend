package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
import com.petlogue.duopetbackend.board.model.dto.Board;
import com.petlogue.duopetbackend.board.model.dto.Comments;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentsRepository commentsRepository;
    private final String ACTIVE_STATUS = "ACTIVE";

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 게시글 수 조회
    public int countFreeBoardByKeywordOrDate(String keyword, Date date) {
        // 키워드만 있는 경우
        if (keyword != null && !keyword.trim().isEmpty() && date == null) {
            return boardRepository.countByCategoryAndContentTypeAndTitleContainingAndStatus("자유", "board", keyword, ACTIVE_STATUS);

            // 날짜만 있는 경우
        } else if ((keyword == null || keyword.trim().isEmpty()) && date != null) {
            return boardRepository.countByCategoryAndContentTypeAndCreatedAtBetweenAndStatus(
                    "자유", "board", getStartOfDay(date), getEndOfDay(date), ACTIVE_STATUS);

            // 키워드 + 날짜 둘 다 없음 (전체)
        } else {
            return boardRepository.countByCategoryAndContentTypeAndStatus("자유", "board", ACTIVE_STATUS);
        }
    }

    // 전체 게시물 목록
    public List<Board> getAllFreeBoards() {
        List<BoardEntity> entities = boardRepository.findByContentTypeAndCategoryAndStatus("board", "자유", ACTIVE_STATUS);

        List<Board> boardList = new ArrayList<>();
        for (BoardEntity entity : entities) {
            boardList.add(entity.toDto());
        }

        return boardList;
    }

    // 게시글 목록 조회
    public ArrayList<Board> selectByKeywordOrDate(String keyword, Date date, Pageable pageable) {
        Page<BoardEntity> page;

        if (keyword != null && !keyword.trim().isEmpty() && date == null) {
            // 키워드만 있을 때
            page = boardRepository.findByCategoryAndContentTypeAndTitleContainingAndStatus("자유", "board", keyword, ACTIVE_STATUS, pageable);
        } else if ((keyword == null || keyword.trim().isEmpty()) && date != null) {
            // 날짜만 있을 때
            page = boardRepository.findByCategoryAndContentTypeAndCreatedAtBetweenAndStatus("자유", "board", getStartOfDay(date), getEndOfDay(date), ACTIVE_STATUS, pageable);
        } else {
            // 전체 목록
            page = boardRepository.findByCategoryAndContentTypeAndStatus("자유", "board", ACTIVE_STATUS, pageable);
        }

        ArrayList<Board> list = new ArrayList<>();
        for (BoardEntity entity : page) {
            list.add(entity.toDto());
        }

        return list;
    }

    // Date로 저장되어 있어 createdAt 이 부분이 시분초까지 저장
    // 프런트에서 yyyy-MM-dd 형식의 날짜만 보내도 저장되어 있는 것이
    // 시분초까지 저장되어 정확한 날짜 판단을 위해 사용함
    private Date getStartOfDay(Date date) {
        return Date.from(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
    }

    private Date getEndOfDay(Date date) {
        return Date.from(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    // 게시물 상세보기
    @Transactional(readOnly = true)
    public Board getBoardDetail(Number id) {
        return boardRepository.findByContentIdAndStatus(id.longValue(), ACTIVE_STATUS)
                .map(BoardEntity::toDto)
                .orElse(null);
    }
    // 관리자용 상세보기
    @Transactional(readOnly = true)
    public Board getBoardDetailForAdmin(Long boardId) {
        return boardRepository.findById(boardId) // 기존 findById 사용
                .map(BoardEntity::toDto)
                .orElse(null);
    }
    public Comments getCommentDetailForAdmin(Long commentId) {
        Optional<CommentsEntity> commentsEntityOptional = commentsRepository.findById(commentId);
        // CommentsEntity를 Comments DTO로 변환하여 반환
        return commentsEntityOptional.map(CommentsEntity::toDto).orElse(null);
    }

    // 게시글 수정
    public void updateBoard(Long id, Board updatedDto, MultipartFile file) {
        BoardEntity board = boardRepository.findByContentIdAndStatus (id, ACTIVE_STATUS)
                .orElseThrow(() -> new IllegalArgumentException("수정할 수 있는 상태의 게시글이 아닙니다."));

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

    // 페이징 리스트
    public ArrayList<Board> selectList(Pageable pageable) {
        Page<BoardEntity> page = boardRepository.findByCategoryAndStatus("자유", ACTIVE_STATUS, pageable);
        ArrayList<Board> list = new ArrayList<>();
        for (BoardEntity entity : page) {
            list.add(entity.toDto());
        }
        return list;
    }

}
