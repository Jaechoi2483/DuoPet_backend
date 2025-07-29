package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.consultation.model.dto.ApiResponse;
import com.petlogue.duopetbackend.consultation.model.dto.VetProfileDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetProfile;
import com.petlogue.duopetbackend.consultation.model.service.VetProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/consultation/vet-profiles")
@RequiredArgsConstructor
public class VetProfileController {
    
    private final VetProfileService vetProfileService;
    
    /**
     * 수의사 프로필 생성
     * POST /api/consultation/vet-profiles
     */
    @PostMapping
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<VetProfile>> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VetProfileDto profileDto) {
        
        try {
            // TODO: Get vetId from authenticated user
            Long vetId = 1L; // Placeholder - should get from auth context
            
            VetProfile profile = vetProfileService.createVetProfile(vetId, profileDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("수의사 프로필이 생성되었습니다.", profile));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating vet profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("프로필 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 수의사 프로필 조회
     * GET /api/consultation/vet-profiles/{vetId}
     */
    @GetMapping("/{vetId}")
    public ResponseEntity<ApiResponse<VetProfile>> getProfile(@PathVariable Long vetId) {
        try {
            VetProfile profile = vetProfileService.getVetProfile(vetId);
            return ResponseEntity.ok(ApiResponse.success(profile));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("수의사 프로필을 찾을 수 없습니다."));
        }
    }
    
    /**
     * 수의사 프로필 조회 - /vet/{vetId} 경로 지원
     * GET /api/consultation/vet-profiles/vet/{vetId}
     */
    @GetMapping("/vet/{vetId}")
    public ResponseEntity<ApiResponse<VetProfile>> getProfileByVetPath(@PathVariable Long vetId) {
        log.info("getProfileByVetPath called with vetId: {}", vetId);
        try {
            VetProfile profile = vetProfileService.getVetProfile(vetId);
            log.info("VetProfile found - vetId: {}, isOnline: {}", vetId, profile.getIsOnline());
            return ResponseEntity.ok(ApiResponse.success(profile));
            
        } catch (IllegalArgumentException e) {
            log.error("VetProfile not found for vetId: {}", vetId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("수의사 프로필을 찾을 수 없습니다."));
        }
    }
    
    /**
     * 수의사 프로필 수정
     * PUT /api/consultation/vet-profiles/{vetId}
     */
    @PutMapping("/{vetId}")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<VetProfile>> updateProfile(
            @PathVariable Long vetId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VetProfileDto profileDto) {
        
        try {
            // TODO: Verify that the authenticated user is the vet
            
            VetProfile updatedProfile = vetProfileService.updateVetProfile(vetId, profileDto);
            return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다.", updatedProfile));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 상담 가능한 수의사 목록 조회
     * GET /api/consultation/vet-profiles/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<VetProfile>>> getAvailableVets(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) Boolean onlineOnly) {
        
        log.info("getAvailableVets API called - page: {}, size: {}, specialty: {}, onlineOnly: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), specialty, onlineOnly);
        
        Page<VetProfile> vets;
        if (specialty != null || Boolean.TRUE.equals(onlineOnly)) {
            vets = vetProfileService.getFilteredVets(pageable, specialty, onlineOnly);
        } else {
            vets = vetProfileService.getAvailableVets(pageable);
        }
        
        log.info("getAvailableVets API result - total: {}, content size: {}", vets.getTotalElements(), vets.getContent().size());
        
        // 캐시 제어 헤더 추가 - 브라우저가 데이터를 캐시하지 않도록 설정
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(ApiResponse.success(vets));
    }
    
    /**
     * 온라인 상태인 수의사 목록 조회
     * GET /api/consultation/vet-profiles/online
     */
    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<VetProfile>>> getOnlineVets() {
        List<VetProfile> onlineVets = vetProfileService.getOnlineVets();
        return ResponseEntity.ok(ApiResponse.success(onlineVets));
    }
    
    /**
     * 테스트용 - 모든 수의사 프로필 조회
     * GET /api/consultation/vet-profiles/test/all
     */
    @GetMapping("/test/all")
    public ResponseEntity<ApiResponse<List<VetProfile>>> getAllProfiles() {
        log.info("Test API - Getting all vet profiles");
        List<VetProfile> allProfiles = vetProfileService.getAllProfiles();
        log.info("Test API - Found {} profiles", allProfiles.size());
        return ResponseEntity.ok(ApiResponse.success(allProfiles));
    }
    
    /**
     * 수의사 검색
     * GET /api/consultation/vet-profiles/search?keyword=
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VetProfile>>> searchVets(
            @RequestParam String keyword) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("검색어를 입력해주세요."));
        }
        
        List<VetProfile> results = vetProfileService.searchVets(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    /**
     * 평점 높은 수의사 목록 조회
     * GET /api/consultation/vet-profiles/top-rated
     */
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<List<VetProfile>>> getTopRatedVets(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<VetProfile> topVets = vetProfileService.getTopRatedVets(limit);
        return ResponseEntity.ok(ApiResponse.success(topVets));
    }
    
    /**
     * 수의사 온라인 상태 업데이트
     * PATCH /api/consultation/vet-profiles/{vetId}/online-status
     */
    @PatchMapping("/{vetId}/online-status")
    @PreAuthorize("hasAuthority('VET') or hasAuthority('vet')")
    public ResponseEntity<ApiResponse<Void>> updateOnlineStatus(
            @PathVariable Long vetId,
            @RequestParam boolean isOnline) {
        
        try {
            log.info("온라인 상태 변경 요청 - vetId: {}, isOnline: {}", vetId, isOnline);
            vetProfileService.updateOnlineStatus(vetId, isOnline);
            String message = isOnline ? "온라인 상태로 변경되었습니다." : "오프라인 상태로 변경되었습니다.";
            log.info("온라인 상태 변경 성공 - {}", message);
            return ResponseEntity.ok(ApiResponse.success(message, null));
            
        } catch (Exception e) {
            log.error("Error updating online status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("상태 업데이트 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 수의사 상담 가능 여부 업데이트
     * PATCH /api/consultation/vet-profiles/{vetId}/availability
     */
    @PatchMapping("/{vetId}/availability")
    @PreAuthorize("hasAuthority('VET')")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable Long vetId,
            @RequestParam boolean isAvailable) {
        
        try {
            vetProfileService.updateAvailability(vetId, isAvailable);
            String message = isAvailable ? "상담 가능 상태로 변경되었습니다." : "상담 불가 상태로 변경되었습니다.";
            return ResponseEntity.ok(ApiResponse.success(message, null));
            
        } catch (Exception e) {
            log.error("Error updating availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("상태 업데이트 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 상담 가능한 수의사 목록과 상담 상태 조회
     * GET /api/consultation/vet-profiles/available-with-status
     */
    @GetMapping("/available-with-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableVetsWithStatus(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("getAvailableVetsWithStatus API called - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Map<String, Object> result = vetProfileService.getAvailableVetsWithStatus(pageable);
        
        // 첫 번째 수의사의 평점 로그 출력
        @SuppressWarnings("unchecked")
        Page<VetProfile> vetsPage = (Page<VetProfile>) result.get("vets");
        if (vetsPage != null && !vetsPage.getContent().isEmpty()) {
            VetProfile firstVet = vetsPage.getContent().get(0);
            log.info("First vet profile - vetId: {}, name: {}, ratingAvg: {}, ratingCount: {}", 
                    firstVet.getVet().getVetId(), 
                    firstVet.getVet().getName(), 
                    firstVet.getRatingAvg(), 
                    firstVet.getRatingCount());
        }
        
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(ApiResponse.success(result));
    }
}