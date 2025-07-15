package com.petlogue.duopetbackend.pet.controller;

import com.petlogue.duopetbackend.pet.model.dto.PetDto;
import com.petlogue.duopetbackend.pet.model.service.PetService;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pet")
@Slf4j
@CrossOrigin
public class PetController {

    private final PetService petService;
    private final UserRepository userRepository;

    /**
     * 회원가입 단계에서 반려동물 등록 처리
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerPet(
            @RequestPart("data") PetDto petDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            Optional<UserEntity> optionalUser = userRepository.findById(petDto.getUserId());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body("유효하지 않은 사용자입니다.");
            }

            // DTO에 파일 직접 세팅
            petDto.setFile(file);

            petService.registerPet(petDto, optionalUser.get());

            return ResponseEntity.ok("반려동물 등록 완료");
        } catch (Exception e) {
            log.error("[반려동물 등록 실패]", e);
            return ResponseEntity.internalServerError().body("등록 중 오류가 발생했습니다.");
        }
    }
}
