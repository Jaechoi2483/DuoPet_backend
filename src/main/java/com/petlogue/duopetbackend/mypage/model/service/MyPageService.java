package com.petlogue.duopetbackend.mypage.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.BookmarkEntity;
import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.BookmarkRepository;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
import com.petlogue.duopetbackend.board.jpa.repository.LikeRepository;
import com.petlogue.duopetbackend.mypage.model.dto.MyBookmarkDto;
import com.petlogue.duopetbackend.mypage.model.dto.MyCommentDto;
import com.petlogue.duopetbackend.mypage.model.dto.MyLikeDto;
import com.petlogue.duopetbackend.mypage.model.dto.MyPostDto;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MyPageService {

    private final BoardRepository boardRepository;
    private final CommentsRepository commentsRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 특정 사용자가 작성한 게시글 목록을 최신순으로 조회
     */
    public List<MyPostDto> findMyPosts(Long userId) {
        List<BoardEntity> boards = boardRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return boards.stream()
                .filter(board -> board.getCategory() != null)
                .map(MyPostDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 작성한 댓글 전체 조회 (게시글 정보 포함)
     */
    public List<MyCommentDto> findMyComments(Long userId) {
        return commentsRepository.findMyCommentsByUserId(userId);
    }

    public List<MyLikeDto> getMyLikedBoards(Long userId) {
        List<LikeEntity> likes = likeRepository.findAllByUserId(userId);

        return likes.stream()
                .map(like -> boardRepository.findById(like.getTargetId())
                        .map(MyLikeDto::from)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID를 기준으로 북마크한 게시글 목록을 조회하고 DTO로 변환
     */
    public List<MyBookmarkDto> getMyBookmarks(Long userId) {
        List<BookmarkEntity> bookmarks = bookmarkRepository.findAllByUserId(userId);

        return bookmarks.stream()
                .map(bm -> boardRepository.findById(bm.getContentId())
                        .map(MyBookmarkDto::from)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void verifyAndResetPassword(String loginId, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (!passwordEncoder.matches(currentPassword, user.getUserPwd())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 암호화 후 저장
        String encodedPwd = passwordEncoder.encode(newPassword);
        user.setUserPwd(encodedPwd);
        userRepository.save(user);
    }

}
