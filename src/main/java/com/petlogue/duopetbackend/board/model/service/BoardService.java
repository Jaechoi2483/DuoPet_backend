package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
import com.petlogue.duopetbackend.board.model.dto.Board;
import com.petlogue.duopetbackend.board.model.dto.Comments;
import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
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

import static kotlin.reflect.jvm.internal.impl.builtins.StandardNames.FqNames.list;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentsRepository commentsRepository;
    private final String ACTIVE_STATUS = "ACTIVE";

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 게시글 수 조회 (카테고리/타입 포함)
    public int countBoardsByKeywordOrDate(String category, String contentType, String keyword, Date date) {
        if (keyword != null && !keyword.trim().isEmpty() && date == null) {
            return boardRepository.countByCategoryAndContentTypeAndTitleContainingAndStatus(category, contentType, keyword, ACTIVE_STATUS);
        } else if ((keyword == null || keyword.trim().isEmpty()) && date != null) {
            return boardRepository.countByCategoryAndContentTypeAndCreatedAtBetweenAndStatus(category, contentType, getStartOfDay(date), getEndOfDay(date), ACTIVE_STATUS);
        } else {
            return boardRepository.countByCategoryAndContentTypeAndStatus(category, contentType, ACTIVE_STATUS);
        }
    }

    // 전체 게시물 목록
    public List<Board> getAllBoardsByCategory(String category, String contentType) {
        List<BoardEntity> entities = boardRepository.findByContentTypeAndCategoryAndStatus(contentType, category, ACTIVE_STATUS);

        List<Board> boardList = new ArrayList<>();
        for (BoardEntity entity : entities) {
            Board board = entity.toDto();
            userRepository.findById(board.getUserId())
                    .ifPresent(user -> board.setNickname(user.getNickname()));
            boardList.add(board);
        }

        return boardList;
    }

    // 게시글 목록 조회 (카테고리/타입 포함)
    public ArrayList<Board> selectBoardsByKeywordOrDate(String category, String contentType, String keyword, Date date, Pageable pageable) {
        Page<BoardEntity> page;

        if (keyword != null && !keyword.trim().isEmpty() && date == null) {
            page = boardRepository.findByCategoryAndContentTypeAndTitleContainingAndStatus(category, contentType, keyword, ACTIVE_STATUS, pageable);
        } else if ((keyword == null || keyword.trim().isEmpty()) && date != null) {
            page = boardRepository.findByCategoryAndContentTypeAndCreatedAtBetweenAndStatus(category, contentType, getStartOfDay(date), getEndOfDay(date), ACTIVE_STATUS, pageable);
        } else {
            page = boardRepository.findByCategoryAndContentTypeAndStatus(category, contentType, ACTIVE_STATUS, pageable);
        }

        ArrayList<Board> list = new ArrayList<>();
        for (BoardEntity entity : page) {
            Board board = entity.toDto();
            userRepository.findById(board.getUserId())
                    .ifPresent(user -> board.setNickname(user.getNickname()));
            list.add(board);
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

    // 게시글 ID와 카테고리, 타입(board)이 모두 일치하는 게시글을 조회
    @Transactional(readOnly = true)
    public Board getBoardDetailByCategoryAndId(String category, String contentType, Long id) {
        Optional<BoardEntity> optional = boardRepository.findById(id);

        if (optional.isPresent()) {
            BoardEntity entity = optional.get();

            if (entity.getCategory().equals(category) && entity.getContentType().equals(contentType)
                    && entity.getStatus().equals(ACTIVE_STATUS)) {

                Board board = entity.toDto();

                userRepository.findById(board.getUserId())
                        .ifPresent(user -> board.setNickname(user.getNickname()));

                return board;
            }
        }

        return null;
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


    // 좋아요 TOP3 (카테고리/타입 포함)
    public List<Board> getTopLikedBoards(String category, String contentType) {
        List<Object[]> results = boardRepository.findTop3LikedByCategory(category, contentType, PageRequest.of(0, 3));
        List<Board> list = new ArrayList<>();

        for (Object[] row : results) {
            Long contentId = ((Number) row[0]).longValue();
            Long userId = ((Number) row[2]).longValue();
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            Optional<BoardEntity> boardOpt = boardRepository.findById(contentId);
            if (boardOpt.isPresent()) {
                Board board = boardOpt.get().toDto();
                board.setNickname(userOpt.map(UserEntity::getNickname).orElse("알 수 없음"));
                list.add(board);
            }
        }
        return list;
    }

    // 조회수 TOP3 (카테고리/타입 포함)
    public List<Board> getTopViewedBoards(String category, String contentType) {
        List<Object[]> results = boardRepository.findTop3ViewedByCategory(category, contentType, PageRequest.of(0, 3));
        List<Board> list = new ArrayList<>();

        for (Object[] row : results) {
            Long contentId = ((Number) row[0]).longValue();
            Long userId = ((Number) row[2]).longValue();
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            Optional<BoardEntity> boardOpt = boardRepository.findById(contentId);
            if (boardOpt.isPresent()) {
                Board board = boardOpt.get().toDto();
                board.setNickname(userOpt.map(UserEntity::getNickname).orElse("알 수 없음"));
                list.add(board);
            }
        }
        return list;
    }

    // 좋아요 TOP 3 게시글 조회 (메인에서)
    public List<Board> findTopLikedBoards() {
        List<String> categories = List.of("free", "review", "tip", "question");
        List<Object[]> results = boardRepository.findTop3LikedForMain(categories, PageRequest.of(0, 3));
        List<Board> list = new ArrayList<>();

        for (Object[] row : results) {
            Long contentId = ((Number) row[0]).longValue();  // 게시글 ID
            Long userId = ((Number) row[2]).longValue();     // 작성자 ID

            Optional<BoardEntity> boardOpt = boardRepository.findById(contentId);
            Optional<UserEntity> userOpt = userRepository.findById(userId);

            if (boardOpt.isPresent()) {
                Board board = boardOpt.get().toDto();
                board.setNickname(userOpt.map(UserEntity::getNickname).orElse("알 수 없음"));
                list.add(board);
            }
        }

        return list;
    }

    // 조회수 TOP 3 게시글 조회 (메인에서)
    public List<Board> findTopViewedBoards() {
        List<String> categories = List.of("free", "review", "tip", "question");
        List<Object[]> results = boardRepository.findTop3ViewedForMain(categories, PageRequest.of(0, 3));
        List<Board> list = new ArrayList<>();


        for (Object[] row : results) {
            Long contentId = ((Number) row[0]).longValue();  // 게시글 ID
            Long userId = ((Number) row[2]).longValue();     // 작성자 ID

            Optional<BoardEntity> boardOpt = boardRepository.findById(contentId);
            Optional<UserEntity> userOpt = userRepository.findById(userId);

            if (boardOpt.isPresent()) {
                Board board = boardOpt.get().toDto();
                board.setNickname(userOpt.map(UserEntity::getNickname).orElse("알 수 없음"));
                list.add(board);
            }
        }

        return list;
    }

    // 공통 페이징 리스트
    public ArrayList<Board> selectListByCategory(String category, Pageable pageable) {
        Page<BoardEntity> page = boardRepository.findByCategoryAndStatus(category, ACTIVE_STATUS, pageable);
        ArrayList<Board> list = new ArrayList<>();
        for (BoardEntity entity : page) {
            list.add(entity.toDto());
        }
        return list;
    }

}
