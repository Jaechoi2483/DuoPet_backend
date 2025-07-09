package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.model.dto.Qna;
import com.petlogue.duopetbackend.admin.model.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/qna")
public class QnaController {

    private final QnaService qnaService;

    @GetMapping
    public ResponseEntity<Page<Qna>> getQnaList(Pageable pageable) {
        Page<Qna> qnaPage = qnaService.findAllQna(pageable);
        return ResponseEntity.ok(qnaPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Qna> getQnaDetail(@PathVariable("id") int contentId) {
        Qna qnaDetail = qnaService.findQnaDetail(contentId);
        return ResponseEntity.ok(qnaDetail);
    }
}
