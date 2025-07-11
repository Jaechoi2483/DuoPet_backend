package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.jpa.entity.FaqEntity;
import com.petlogue.duopetbackend.admin.model.dto.Faq;
import com.petlogue.duopetbackend.admin.model.service.FaqService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/faq")
public class FaqController {

    private final FaqService faqService;
    private final UserRepository userRepository;


    @GetMapping
    public ResponseEntity<Page<Faq>> getFaqs(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "faqId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Faq> faqPage = faqService.findFaqs(keyword, pageable);
        return ResponseEntity.ok(faqPage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Faq> updateFaq(
            @PathVariable("id") int faqId, // 경로에서 수정할 FAQ의 ID를 받음
            @RequestBody Faq updateRequest) { // 요청 본문에서 수정할 내용을 받음

        Faq updatedFaq = faqService.updateFaq(faqId, updateRequest);
        return ResponseEntity.ok(updatedFaq);
    }

    @PostMapping
    public ResponseEntity<?> createFaq(
            @RequestBody Faq requestDto,
            Authentication authentication) {

        // 1. 관리자 권한 확인 (SecurityConfig에서도 하지만, 컨트롤러에서 한번 더 명시적으로 확인)
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("admin") || a.getAuthority().equals("admin")); // 실제 역할명에 맞게 조정

        if (!isAdmin) {
            log.warn("FAQ 등록 시도 - 권한 없음: 사용자 ID = {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FAQ 등록 권한이 없습니다.");
        }

        // 2. 현재 로그인한 관리자의 userId 조회
        String adminLoginId = authentication.getName();
        UserEntity adminUser = userRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new IllegalStateException("관리자 사용자 정보를 찾을 수 없습니다."));
        Integer adminUserId = adminUser.getUserId().intValue();

        // 3. 요청 DTO 유효성 검사 (간단하게)
        if (requestDto.getQuestion() == null || requestDto.getQuestion().trim().isEmpty() ||
                requestDto.getAnswer() == null || requestDto.getAnswer().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("질문과 답변 내용을 모두 입력해주세요.");
        }

        try {
            FaqEntity savedFaqEntity = faqService.createFaq(requestDto, adminUserId);
            log.info("FAQ 등록 성공: FAQ ID = {}", savedFaqEntity.getFaqId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFaqEntity.toDto()); // 등록된 FAQ DTO 반환
        } catch (Exception e) {
            log.error("FAQ 등록 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAQ 등록 중 서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> faqDeleteMethod(@PathVariable("id") int faqId) {

        // 서비스 호출에 성공하면 true, 실패하면 false가 반환된다고 가정
        if (faqService.deleteFaq(faqId)) {
            // 삭제 성공 시 200 OK 와 함께 빈 본문 반환
            return ResponseEntity.ok().build();
        } else {
            // 해당 ID의 FAQ가 없거나 삭제에 실패한 경우
            return ResponseEntity.notFound().build();
        }
    }
}
