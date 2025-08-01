package com.petlogue.duopetbackend.board.controller;



import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.repository.*;

import com.petlogue.duopetbackend.board.model.dto.Board;
import com.petlogue.duopetbackend.board.model.dto.Bookmark;
import com.petlogue.duopetbackend.board.model.dto.Like;
import com.petlogue.duopetbackend.board.model.dto.Report;
import com.petlogue.duopetbackend.board.model.service.BoardService;
import com.petlogue.duopetbackend.board.model.service.BookmarkService;
import com.petlogue.duopetbackend.board.model.service.LikeService;
import com.petlogue.duopetbackend.board.model.service.ReportService;
import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.common.Paging;
import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/board")
@CrossOrigin
public class BoardController {

    private final BoardService boardService;

    private final BookmarkService bookmarkService;

    private final LikeService likeService;

    private final BoardRepository boardRepository;

    private final UserRepository userRepository;

    private final LikeRepository likeRepo;

    private final CommentsRepository commentsRepository;

    private final BookmarkRepository bookmarkRepository;

    private final ReportRepository reportRepository;

    private final ReportService reportService;

    private final JWTUtil jwtUtil;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 전체 자유 게시판 목록 조회
    @GetMapping("/{category}")
    public List<Board> getBoardListByCategory(@PathVariable String category) {
        log.info("{} 게시판 전체 조회 요청", category);
        return boardService.getAllBoardsByCategory(category, "board");
    }

    // 게시글 상세 조회
    @GetMapping("/{category}/detail/{id}")
    public Board getBoardDetail(@PathVariable String category, @PathVariable Long id) {
        log.info("카테고리별 게시글 상세 조회 요청: category={}, id={}", category, id);
        Board board = boardService.getBoardDetailByCategoryAndId(category, "board", id);

        if (board == null) {
            log.warn("해당 게시글이 존재하지 않거나 카테고리가 다릅니다. id: {}, category: {}", id, category);
        }

        return board;
    }

    @PostMapping("/{category}/write")
    public ResponseEntity<?> insertBoard(
            @PathVariable String category,
            @ModelAttribute Board board,
            @RequestParam(name = "ofile", required = false) MultipartFile mfile
    ) {
        log.info("게시글 등록 요청: {}", board);

        board.setCategory(category);
        board.setContentType("board");

        // 1. 파일 저장 경로 생성
        String savePath = uploadDir + "/board";
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 파일 저장 처리
        if (mfile != null && !mfile.isEmpty()) {
            String originalName = mfile.getOriginalFilename();
            String renamedName = FileNameChange.change(originalName, "yyyyMMddHHmmss");

            try {
                mfile.transferTo(new File(savePath, renamedName));
                board.setOriginalFilename(originalName);
                board.setRenameFilename(renamedName);
            } catch (Exception e) {
                log.error("파일 저장 실패", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 실패");
            }
        }

        // 3. 날짜/조회수 초기화
        board.setCreatedAt(new Date());  // 작성일 지정
        board.setViewCount(0);           // 조회수 초기화
        board.setLikeCount(0);           // 좋아요 초기화

        // 4. DB 저장
        try {
            BoardEntity entity = board.toEntity();
            boardRepository.save(entity);
            return ResponseEntity.ok("게시글 등록 완료");
        } catch (Exception e) {
            log.error("게시글 저장 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB 저장 실패");
        }
    }

    // 게시물 수정
    @PutMapping("/{category}/{id}")
    public ResponseEntity<?> updateBoard(
            @PathVariable String category,
            @PathVariable Long id,
            @ModelAttribute Board board,
            @RequestParam(name = "ofile", required = false) MultipartFile mfile,
            HttpServletRequest request
    ) {
        log.info("게시글 수정 요청: category={}, {}", category, id);

        try {
            BoardEntity existing = boardRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

            // 1. 토큰에서 로그인된 사용자 ID 추출
            Long userId = jwtUtil.getUserIdFromRequest(request);
            log.info("수정 요청 사용자 ID: {}", userId);

            // 2. 게시글 작성자와 요청자 비교
            if (!existing.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
            }

            // category 수동 주입
            board.setCategory(category.trim());


            // 제목/내용/카테고리/태그 업데이트
            existing.setTitle(board.getTitle());
            existing.setContentBody(board.getContentBody());
            existing.setCategory(board.getCategory());
            existing.setTags(board.getTags());

            // 파일 처리
            if (mfile != null && !mfile.isEmpty()) {
                String savePath = uploadDir + "/board";
                File dir = new File(savePath);
                if (!dir.exists()) dir.mkdirs();

                String originalName = mfile.getOriginalFilename();
                String renamedName = FileNameChange.change(originalName, "yyyyMMddHHmmss");

                try {
                    mfile.transferTo(new File(savePath, renamedName));
                    existing.setOriginalFilename(originalName);
                    existing.setRenameFilename(renamedName);
                } catch (Exception e) {
                    log.error("파일 저장 실패", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 실패");
                }
            }

            boardRepository.save(existing);
            return ResponseEntity.ok("게시글 수정 완료");

        } catch (Exception e) {
            log.error("게시글 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 수정 중 오류 발생");
        }
    }

    // 게시물 삭제
    @DeleteMapping("/{category}/{id}")
    public ResponseEntity<?> deleteBoard(
            @PathVariable String category,
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            // 1. JWT에서 사용자 ID 추출
            Long userId = jwtUtil.getUserIdFromRequest(request);
            log.info("게시글 삭제 요청: 게시글 ID={}, 카테고리={}, 사용자 ID={}", id, category, userId);
            log.info("Authorization 헤더: {}", request.getHeader("Authorization"));
            log.info("파싱된 사용자 ID: {}", userId);

            // 2. 게시물 존재 여부 확인
            Optional<BoardEntity> optional = boardRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글이 존재하지 않습니다.");
            }

            BoardEntity board = optional.get();

            // 3. 카테고리 일치 & 게시물 작성자와 요청자 일치 여부 확인
            if (!board.getCategory().equalsIgnoreCase(category.trim())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("카테고리 정보가 일치하지 않습니다.");
            }

            if (!board.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }

            // 북마크 먼저 삭제
            bookmarkRepository.deleteAllByContentId(id);

            // 댓글 삭제
            commentsRepository.deleteAllByContentId(id);

            // 좋아요 삭제
            likeRepo.deleteAllByTargetIdAndTargetType(id, "board");

            // 신고 삭제
            reportRepository.deleteAllByTargetIdAndTargetType(id, "board");

            // 4. 첨부파일 삭제 (선택)
            if (board.getRenameFilename() != null) {
                File file = new File(uploadDir + "/board/" + board.getRenameFilename());
                if (file.exists()) {
                    file.delete();
                }
            }

            boardRepository.deleteById(id);
            return ResponseEntity.ok("게시글 삭제 완료");

        } catch (Exception e) {
            log.error("게시글 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 삭제 중 오류 발생");
        }
    }

    // 좋아요 TOP3
    @GetMapping("/{category}/top-liked")
    public List<Board> getTopLikedBoards(@PathVariable String category) {
        log.info("TOP 좋아요 게시글 요청: {}", category);
        return boardService.getTopLikedBoards(category, "board");
    }


    // 조회수 TOP3
    @GetMapping("/{category}/top-viewed")
    public List<Board> getTopViewedBoards(@PathVariable String category) {
        return boardService.getTopViewedBoards(category, "board");
    }

    // 좋아요 순 TOP 3 조회 (메인에서)
    @GetMapping("/maintop-liked")
    public ResponseEntity<List<Board>> getTopLiked() {
        List<Board> result = boardService.findTopLikedBoards()
                .stream()
                .map(dto -> {
                    dto.setNickname(
                            userRepository.findById(dto.getUserId())
                                    .map(UserEntity::getNickname)
                                    .orElse("알 수 없음")
                    );
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(result);
    }


    // 조회수 순 TOP 3 조회 (메인에서)
    @GetMapping("/maintop-viewed")
    public ResponseEntity<List<Board>> getTopViewed() {
        List<Board> result = boardService.findTopViewedBoards()
                .stream()
                .map(dto -> {
                    dto.setNickname(
                            userRepository.findById(dto.getUserId())
                                    .map(UserEntity::getNickname)
                                    .orElse("알 수 없음")
                    );
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    // 게시판 목록 & 페이징 정보 조회
    @GetMapping("/{category}/list")
    public Map<String, Object> boardList(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date
    ) {
        log.info("자유게시판 목록 요청 (page={}, keyword={}, sort={}, date={})", category, page, keyword, sort, date);

        // 페이징 + 정렬
        Sort sorting = sort.equals("title")
                ? Sort.by("title").ascending()
                : Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page - 1, limit, sorting);

        // 게시글 수 조회
        int count = boardService.countBoardsByKeywordOrDate(category, "board", keyword, date);
        Paging paging = new Paging(count, limit, page, "/" + category + "/list");

        // 게시글 목록 조회
        List<Board> list = boardService.selectBoardsByKeywordOrDate(category, "board", keyword, date, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("paging", paging);
        return result;
    }

    // 조회수 증가용 API (GET 방식, 비회원 가능)
    @GetMapping("/view-count")
    public ResponseEntity<?> increaseViewCount(@RequestParam Long id) {
        try {
            log.info("비회원 포함 조회수 증가 요청: 게시글 ID = {}", id);
            Optional<BoardEntity> optional = boardRepository.findById(id);

            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글이 존재하지 않습니다.");
            }

            BoardEntity board = optional.get();
            board.setViewCount(board.getViewCount() + 1);
            boardRepository.save(board); // 변경사항 저장

            return ResponseEntity.ok().build(); // 응답 본문 없이 200 OK
        } catch (Exception e) {
            log.error("조회수 증가 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회수 증가 중 오류 발생");
        }
    }

    // 좋아요 등록
    @PostMapping("/{category}/like/{id}")
    public ResponseEntity<?> toggleBoardLike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("Controller 도달 - request.getAttribute(userId): {}", request.getAttribute("userId"));

        if (userId == null) {
            log.error("좋아요 토글 요청 - 인증 실패, userId가 null");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "사용자 인증 실패"));
        }

        log.info("좋아요 토글 요청 - userId: {}, contentId: {}", userId, id);

        Like result = likeService.toggleBoardLike(userId, id);
        log.info("좋아요 토글 결과 - liked: {}", result.isLiked());

        return ResponseEntity.ok(Map.of("liked", result.isLiked(), "message", result.isLiked() ? "좋아요 등록됨" : "좋아요 취소됨"));
    }

    // 좋아요 상태 확인
    @GetMapping("/{category}/like/{contentId}/status")
    public ResponseEntity<?> checkLikeStatus(@PathVariable Long contentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("liked", false));
        }

        boolean liked = likeRepo.findByUserIdAndTargetIdAndTargetType(userId, contentId, "board").isPresent();
        return ResponseEntity.ok(Map.of("liked", liked));
    }


    // 북마크 등록
    @PostMapping("/{category}/bookmark/{id}")
    public ResponseEntity<?> toggleBookmark(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("Bookmark Controller 도달 - request.getAttribute(userId): {}", userId);

        if (userId == null) {
            log.error("북마크 토글 요청 - 인증 실패, userId가 null");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "로그인이 필요한 기능입니다."));
        }

        log.info("북마크 토글 요청 - userId: {}, contentId: {}", userId, id);

        Bookmark result = bookmarkService.toggleBookmark(userId, id);
        log.info("북마크 토글 결과 - bookmarked: {}", result.isBookmarked());

        return ResponseEntity.ok(Map.of(
                "bookmarked", result.isBookmarked(),
                "message", result.isBookmarked() ? "북마크 등록됨" : "북마크 해제됨"
        ));
    }

    // 북마크 상태 확인
    @GetMapping("/{category}/bookmark/{contentId}/status")
    public ResponseEntity<?> checkBookmarkStatus(@PathVariable Long contentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("bookmarked", false));
        }

        // findByUserIdAndTargetIdAndTargetType 메서드가 Optional을 반환함
        boolean bookmarked = bookmarkRepository
                .findByUserIdAndContentIdAndTargetType(userId, contentId, "board")
                .isPresent();

        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @PostMapping("/report")
    public ResponseEntity<String> report(@RequestBody Report dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        // 신고 처리 서비스 호출
        try {
            reportService.saveReport(userId, dto);  // 신고 서비스 호출
            return ResponseEntity.ok("신고가 접수되었습니다.");
        } catch (IllegalStateException e) {
            // 중복 신고 시
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 본인 게시글 신고 시, 이 블록에서 메시지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("신고 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("신고 처리 중 오류 발생");
        }
    }

    // 세션에서 사용자 ID 추출
    public Long getUserIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);  // 세션이 없으면 null 반환
        if (session != null) {
            return (Long) session.getAttribute("userId");  // 세션에서 userId 가져오기
        }
        return null;  // 세션에 userId가 없으면 null 반환
    }
}
