package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.model.dto.Qna;
import com.petlogue.duopetbackend.admin.model.service.QnaService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/qna")
public class QnaController {

    private final QnaService qnaService;
    private final UserRepository userRepository;


    @GetMapping
    public ResponseEntity<Page<Qna>> getQnaList(Pageable pageable,
                                                @RequestParam(required = false, defaultValue = "ALL") String status,
                                                Authentication authentication) {

        // 현재 로그인한 사용자의 역할 확인
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User role not found"))
                .getAuthority();

        Page<Qna> qnaPage;

        if ("admin".equals(role)) {
            // 관리자일 경우: 모든 Q&A 목록을 조회
            qnaPage = qnaService.findAllQnaForAdmin(pageable, status);
        } else {
            // 일반 사용자일 경우: 자신의 Q&A 목록만 조회
            String loginId = authentication.getName();
            qnaPage = qnaService.findAllQnaByUser(loginId, pageable, status);
        }

        return ResponseEntity.ok(qnaPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Qna> getQnaDetail(@PathVariable("id") int contentId) {
        Qna qnaDetail = qnaService.findQnaDetail(contentId);
        return ResponseEntity.ok(qnaDetail);
    }
    @PostMapping
    public ResponseEntity<?> createQna(@RequestBody Qna qna, Authentication authentication) {

        // 1. 현재 로그인한 사용자의 ID("user01" 등)를 가져옵니다.
        String loginId = authentication.getName();

        // 2. ID로 User 엔티티를 조회하여 실제 숫자 ID(PK)를 찾습니다.
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Integer userId = user.getUserId().intValue(); // UserEntity의 PK를 가져옴 (타입은 실제에 맞게)

        // 3. 서비스에 DTO와 함께 숫자 userId를 전달합니다.
        int result = qnaService.saveQna(qna, userId);

        if (result > 0) {
            return ResponseEntity.ok().build(); // 성공
        } else {
            return ResponseEntity.status(500).body("게시글 저장 실패"); // 실패
        }
    }

}
