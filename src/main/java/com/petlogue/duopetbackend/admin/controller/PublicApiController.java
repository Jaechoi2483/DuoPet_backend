package com.petlogue.duopetbackend.admin.controller;

import com.petlogue.duopetbackend.admin.model.dto.PublicSummaryDto;
import com.petlogue.duopetbackend.admin.model.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicApiController {

    private final AdminService adminService;

    // 공개용 API 엔드포인트
    @GetMapping("/api/summary/public")
    public ResponseEntity<PublicSummaryDto> getPublicSummary() {
        // 2단계에서 만든 서비스 메소드를 호출합니다.
        PublicSummaryDto summaryData = adminService.getPublicSummaryData();
        return ResponseEntity.ok(summaryData);
    }
}
