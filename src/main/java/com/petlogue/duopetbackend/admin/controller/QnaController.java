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
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<?> createAnswer(
            @PathVariable("qnaId") int qnaId,
            @RequestBody Map<String, String> requestBody, // 답변 내용을 Map으로 받음
            Authentication authentication) {





        // 1. 현재 로그인한 사용자의 역할 확인 (관리자만 가능)
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("admin")); // 실제 관리자 역할명으로 변경 (예: "ROLE_ADMIN")

        if (!isAdmin) {
            log.warn("Q&A 답변 시도 - 권한 없음: 사용자 ID = {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("답변 등록 권한이 없습니다."); // 403 Forbidden
        }

        log.info("--- 백엔드 요청 본문 확인 ---");
        log.info("qnaId: {}", qnaId);
        log.info("requestBody: {}", requestBody); // Map 전체를 로깅
        log.info("requestBody.get(\"content\"): {}", requestBody.get("content")); // content 필드 값 확인
        log.info("---------------------------");

        // 2. 답변 내용 추출 및 유효성 검사
        String content = requestBody.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("답변 내용을 입력해주세요."); // 400 Bad Request
        }

        // 3. 현재 로그인한 관리자의 userId 조회
        String adminLoginId = authentication.getName();
        UserEntity adminUser = userRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new IllegalStateException("관리자 사용자 정보를 찾을 수 없습니다.")); // 이 경우는 발생해서는 안됨
        Integer adminUserId = adminUser.getUserId().intValue();

        // 4. 서비스 호출하여 답변 저장
        try {
            int result = qnaService.saveAnswer(qnaId, content, adminUserId);

            if (result > 0) {
                log.info("Q&A 답변 등록 성공: QnA ID = {}, 관리자 ID = {}", qnaId, adminUserId);
                return ResponseEntity.status(HttpStatus.CREATED).body("답변이 성공적으로 등록되었습니다."); // 201 Created
            } else {
                log.error("Q&A 답변 등록 실패: QnA ID = {}, 관리자 ID = {}", qnaId, adminUserId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("답변 등록에 실패했습니다."); // 500 Internal Server Error
            }
        } catch (IllegalArgumentException e) {
            log.warn("Q&A 답변 등록 실패 - QnA 없음: QnA ID = {}", qnaId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            log.error("Q&A 답변 등록 중 예상치 못한 오류 발생: QnA ID = {}", qnaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다."); // 500 Internal Server Error
        }
    }


}
