package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.model.dto.PublicSummaryDto;
import com.petlogue.duopetbackend.admin.model.dto.ShoppingResponseDto;
import com.petlogue.duopetbackend.admin.model.service.AdminService;
import com.petlogue.duopetbackend.admin.model.service.ShoppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicApiController {
    @Autowired
    private final AdminService adminService;
    @Autowired
    private ShoppingService shoppingService;


    // 공개용 API 엔드포인트
    @GetMapping("/api/summary/public")
    public ResponseEntity<PublicSummaryDto> getPublicSummary() {
        // 2단계에서 만든 서비스 메소드를 호출합니다.
        PublicSummaryDto summaryData = adminService.getPublicSummaryData();
        return ResponseEntity.ok(summaryData);
    }

    @GetMapping("/api/shopping/items")
    public ResponseEntity<ShoppingResponseDto> searchItems(
            @RequestParam(defaultValue = "강아지 간식") String query,
            @RequestParam(defaultValue = "3") Integer display,
            @RequestParam(defaultValue = "1") Integer start) {

        ShoppingResponseDto response = shoppingService.search(query, display, start);
        return ResponseEntity.ok(response);
    }
}
