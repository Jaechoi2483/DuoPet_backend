package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.model.dto.Qna;
import com.petlogue.duopetbackend.admin.model.service.QnaService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

        log.info(" 컨트롤러 수신: page={}, size={}, sort={}, status={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), status);
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User role not found"))
                .getAuthority();

        Page<Qna> qnaPage;

        if ("admin".equals(role)) {
            qnaPage = qnaService.findAllQnaForAdmin(pageable, status);
        } else {
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

        String loginId = authentication.getName();

        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Integer userId = user.getUserId().intValue();

        int result = qnaService.saveQna(qna, userId);

        if (result > 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).body("게시글 저장 실패");
        }
    }

    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<?> createAnswer(
            @PathVariable("qnaId") int qnaId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("admin"));

        if (!isAdmin) {
            log.warn("Q&A 답변 시도 - 권한 없음: 사용자 ID = {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("답변 등록 권한이 없습니다.");
        }

        String content = requestBody.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("답변 내용을 입력해주세요.");
        }

        String adminLoginId = authentication.getName();
        UserEntity adminUser = userRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new IllegalStateException("관리자 사용자 정보를 찾을 수 없습니다."));
        Integer adminUserId = adminUser.getUserId().intValue();
        try {
            int result = qnaService.saveAnswer(qnaId, content, adminUserId);

            if (result > 0) {
                log.info("Q&A 답변 등록 성공: QnA ID = {}, 관리자 ID = {}", qnaId, adminUserId);
                return ResponseEntity.status(HttpStatus.CREATED).body("답변이 성공적으로 등록되었습니다.");
            } else {
                log.error("Q&A 답변 등록 실패: QnA ID = {}, 관리자 ID = {}", qnaId, adminUserId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("답변 등록에 실패했습니다.");
            }
        } catch (IllegalArgumentException e) {
            log.warn("Q&A 답변 등록 실패 - QnA 없음: QnA ID = {}", qnaId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Q&A 답변 등록 중 예상치 못한 오류 발생: QnA ID = {}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }


}
