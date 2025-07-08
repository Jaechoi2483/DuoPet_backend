package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.model.dto.Faq;
import com.petlogue.duopetbackend.admin.model.service.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/faq")
public class FaqController {

    private final FaqService faqService;


    @GetMapping
    public ResponseEntity<List<Faq>> getFaqList() {
        List<Faq> faqList = faqService.findAllFaqs();
        return ResponseEntity.ok(faqList);
    }
}
