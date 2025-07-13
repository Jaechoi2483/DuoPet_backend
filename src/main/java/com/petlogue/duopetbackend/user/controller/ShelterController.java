package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.ShelterRepository;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.ShelterDto;
import com.petlogue.duopetbackend.user.model.service.ShelterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/shelter")
@RequiredArgsConstructor
@CrossOrigin
public class ShelterController {

    private final ShelterService shelterService;
    private final UserRepository userRepository;
    private final ShelterRepository shelterRepository;

    /**
     * [3단계] 인증파일 임시 업로드
     * - 경로: C:/upload_files/shelter/temp
     * - 응답: originalFilename, renameFilename 포함한 ShelterDto
     */
    @PostMapping("/upload-temp")
    public ResponseEntity<Map<String, String>> uploadShelterFileTemp(@RequestParam MultipartFile file) {
        ShelterDto dto = shelterService.processShelterFileUpload(file);

        Map<String, String> response = new HashMap<>();
        response.put("originalFilename", dto.getShelterFileOriginalFilename());
        response.put("renameFilename", dto.getShelterFileRenameFilename());

        return ResponseEntity.ok(response);
    }

    /**
     * [최종 등록] 보호소 등록 (파일 이동 + Entity 저장)
     */
    @PostMapping("/register")
    public String registerShelter(
            @RequestPart("shelterDto") ShelterDto shelterDto,
            @RequestPart("shelterProfileFile") MultipartFile file,
            @RequestParam("loginId") String loginId
    ) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        shelterDto.setShelterProfileFile(file);
        shelterService.registerShelter(user, shelterDto);

        return "보호소 등록 완료 (승인 대기 중)";
    }

    /**
     * 보호소 등록 여부 확인
     */
    @GetMapping("/check")
    public boolean isAlreadyRegistered(@RequestParam("loginId") String loginId) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return shelterRepository.existsByUser(user);
    }
}
