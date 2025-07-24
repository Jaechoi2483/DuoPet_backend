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
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.ShelterDto;
import com.petlogue.duopetbackend.user.model.dto.VetDto;
import com.petlogue.duopetbackend.user.model.service.ShelterService;
import com.petlogue.duopetbackend.user.model.service.UserService;
import com.petlogue.duopetbackend.user.model.service.VetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;
    private final VetService vetService;
    private final ShelterService shelterService;
    private final UserRepository userRepository;

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

    /**
     * 전문가 역할 변경 요청 처리
     */
    @PostMapping("/role/vet")
    public ResponseEntity<String> updateVetRole(@ModelAttribute VetDto vetDto) {
        try {
            vetService.updateVetInfo(vetDto);
            return ResponseEntity.ok("전문가 정보가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("전문가 정보 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 보호소 운영자 역할 변경 요청 처리
     */
    @PostMapping("/role/shelter")
    public ResponseEntity<?> updateShelterRole(@ModelAttribute ShelterDto shelterDto) {
        try {
            shelterService.updateShelterInfo(shelterDto);
            return ResponseEntity.ok("보호소 역할 정보가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("보호소 정보 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/face-images/{userId}")
    public ResponseEntity<Resource> serveFaceImage(@PathVariable Long userId) {
        try {
            // 저장된 파일명 꺼내기 (DB 조회 또는 경로 직접 조합)
            UserEntity user = userRepository.findByUserId(userId);
            if (user == null || user.getFaceRenameFilename() == null) {
                return ResponseEntity.notFound().build();
            }

            // 저장 경로 조합
            String filePath = "C:/upload_files/face/" + user.getFaceRenameFilename();
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // Content-Type을 image로 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/face-delete")
    public ResponseEntity<?> deleteFaceImage(@RequestParam Long userId) {
        try {
            UserEntity user = userRepository.findByUserId(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 없음");
            }

            // 파일 삭제
            String filePath = "C:/upload_files/face/" + user.getFaceRenameFilename();
            File file = new File(filePath);
            if (file.exists()) file.delete();

            // DB 필드 초기화
            user.setFaceOriginalFilename(null);
            user.setFaceRenameFilename(null);
            userRepository.save(user);

            return ResponseEntity.ok("얼굴 이미지 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패");
        }
    }
}
