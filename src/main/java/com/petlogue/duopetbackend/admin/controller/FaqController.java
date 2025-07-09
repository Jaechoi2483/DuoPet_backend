package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.model.dto.Faq;
import com.petlogue.duopetbackend.admin.model.service.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/faq")
public class FaqController {

    private final FaqService faqService;


    @GetMapping
    public ResponseEntity<Page<Faq>> getFaqs(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "faqId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Faq> faqPage = faqService.findFaqs(keyword, pageable);
        return ResponseEntity.ok(faqPage);
    }

    @PutMapping("/faq/{id}")
    public ResponseEntity<Faq> updateFaq(
            @PathVariable("id") int faqId, // 경로에서 수정할 FAQ의 ID를 받음
            @RequestBody Faq updateRequest) { // 요청 본문에서 수정할 내용을 받음

        Faq updatedFaq = faqService.updateFaq(faqId, updateRequest);
        return ResponseEntity.ok(updatedFaq);
    }
}
