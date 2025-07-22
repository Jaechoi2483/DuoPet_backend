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

import java.util.List;
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
    
    /**
     * 사용자의 반려동물 목록 조회
     */
    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getUserPets(@PathVariable Long userId) {
        try {
            List<PetDto> pets = petService.getUserPets(userId);
            return ResponseEntity.ok(pets);
        } catch (Exception e) {
            log.error("[반려동물 목록 조회 실패]", e);
            return ResponseEntity.internalServerError().body("조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 특정 반려동물 상세 정보 조회
     */
    @GetMapping("/{petId}")
    public ResponseEntity<?> getPetDetail(@PathVariable Long petId) {
        try {
            PetDto pet = petService.getPetDetail(petId);
            if (pet == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(pet);
        } catch (Exception e) {
            log.error("[반려동물 상세 조회 실패]", e);
            return ResponseEntity.internalServerError().body("조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 반려동물 정보 수정
     */
    @PutMapping("/update/{petId}")
    public ResponseEntity<?> updatePet(
            @PathVariable Long petId,
            @RequestPart("data") PetDto petDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            // DTO에 파일 직접 세팅
            petDto.setFile(file);
            
            petService.updatePet(petId, petDto);
            
            return ResponseEntity.ok("반려동물 정보가 수정되었습니다.");
        } catch (Exception e) {
            log.error("[반려동물 수정 실패]", e);
            return ResponseEntity.internalServerError().body("수정 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 반려동물 삭제
     */
    @DeleteMapping("/{petId}")
    public ResponseEntity<?> deletePet(@PathVariable Long petId) {
        try {
            petService.deletePet(petId);
            return ResponseEntity.ok("반려동물이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("[반려동물 삭제 실패]", e);
            return ResponseEntity.internalServerError().body("삭제 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 반려동물 프로필 이미지 조회
     */
    @GetMapping("/image/{filename}")
    public org.springframework.core.io.Resource getImage(@PathVariable String filename) throws java.net.MalformedURLException {
        java.io.File file = new java.io.File("C:/upload_files/pet/" + filename);
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toURI());
        return resource;
    }
}

