package com.petlogue.duopetbackend.user.controller;

import java.util.Map;
import java.util.HashMap;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.jpa.repository.VetRepository;
import com.petlogue.duopetbackend.user.model.dto.VetDto;
import com.petlogue.duopetbackend.user.model.service.VetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/vet")
@RequiredArgsConstructor
public class VetController {

    private final VetService vetService;
    private final UserRepository userRepository;
    private final VetRepository vetRepository;

    /**
     * [3단계] 면허증 파일 임시 업로드
     * - 경로: C:/upload_files/vet/temp
     * - 응답: originalFilename, renameFilename 포함한 VetDto
     */
    @PostMapping("/upload-temp")
    public ResponseEntity<Map<String, String>> uploadLicenseTemp(@RequestParam MultipartFile file) {
        VetDto dto = vetService.processVetFileUpload(file);

        Map<String, String> response = new HashMap<>();
        response.put("originalFilename", dto.getVetFileOriginalFilename());
        response.put("renameFilename", dto.getVetFileRenameFilename());

        return ResponseEntity.ok(response);
    }

    /**
     * [최종 등록] 전문가 등록 (면허증 파일 이동 + VetEntity 저장)
     */
    @PostMapping("/register")
    public String registerVet(
            @RequestPart("vetDto") VetDto vetDto,
            @RequestPart("licenseFile") MultipartFile file,
            @RequestParam("loginId") String loginId
    ) {
        // 1. 로그인 ID로 사용자 찾기
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 첨부파일 설정
        vetDto.setLicenseFile(file);

        // 3. 등록 처리
        vetService.registerVet(user, vetDto);

        return "전문가 등록 완료 (승인 대기 중)";
    }

    /**
     * 전문가 등록 여부 확인 (중복 방지)
     */
    @GetMapping("/check")
    public boolean isAlreadyRegistered(@RequestParam("loginId") String loginId) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return vetRepository.existsByUser(user);
    }
}
