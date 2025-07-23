package com.petlogue.duopetbackend.mypage.controller;

import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.model.dto.Bookmark;
import com.petlogue.duopetbackend.board.model.dto.Like;
import com.petlogue.duopetbackend.board.model.service.BookmarkService;
import com.petlogue.duopetbackend.board.model.service.LikeService;
import com.petlogue.duopetbackend.mypage.model.dto.MyBookmarkDto;
import com.petlogue.duopetbackend.mypage.model.dto.MyCommentDto;
import com.petlogue.duopetbackend.mypage.model.dto.MyLikeDto;
import com.petlogue.duopetbackend.mypage.model.dto.MyPostDto;
import com.petlogue.duopetbackend.mypage.model.service.MyPageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;

    /**
     * 마이페이지 - 내가 작성한 게시글 목록 조회
     */
    @GetMapping("/posts")
    public ResponseEntity<List<MyPostDto>> getMyPosts(@RequestParam Long userId) {
        List<MyPostDto> posts = myPageService.findMyPosts(userId);
        return ResponseEntity.ok(posts);
    }

    /**
     * 내가 작성한 댓글 목록 반환
     */
    @GetMapping("/comments")
    public ResponseEntity<?> getMyComments(@RequestAttribute("userId") Long userId) {
        List<MyCommentDto> comments = myPageService.findMyComments(userId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/likes")
    public ResponseEntity<List<MyLikeDto>> getMyLikedBoards(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MyLikeDto> likedBoards = myPageService.getMyLikedBoards(userId);
        return ResponseEntity.ok(likedBoards);
    }

    /**
     * 내가 북마크한 게시글 목록 반환
     */
    @GetMapping("/bookmarks")
    public ResponseEntity<List<MyBookmarkDto>> getMyBookmarks(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<MyBookmarkDto> bookmarks = myPageService.getMyBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * 게시글 좋아요 등록 또는 취소
     */
    @PostMapping("/like/{boardId}")
    public ResponseEntity<Like> toggleBoardLike(@PathVariable Long boardId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Like likeResult = likeService.toggleBoardLike(userId, boardId);
        return ResponseEntity.ok(likeResult);
    }

    /**
     * 게시글 북마크 등록 또는 취소
     */
    @PostMapping("/bookmark/{boardId}")
    public ResponseEntity<Bookmark> toggleBookmark(@PathVariable Long boardId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Bookmark result = bookmarkService.toggleBookmark(userId, boardId);
        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 좋아요 여부 확인
     */
    @GetMapping("/like/check/{contentId}")
    public ResponseEntity<Boolean> isLiked(@PathVariable Long contentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean liked = likeService.isLiked(userId, contentId);
        return ResponseEntity.ok(liked);
    }

    /**
     * 게시글 북마크 여부 확인
     */
    @GetMapping("/bookmark/check/{contentId}")
    public ResponseEntity<Boolean> isBookmarked(@PathVariable Long contentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean bookmarked = bookmarkService.isBookmarked(userId, contentId);
        return ResponseEntity.ok(bookmarked);
    }
}
