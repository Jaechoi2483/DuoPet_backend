// src/main/java/com/petlogue/duopetbackend/pet/model/service/PetService.java

package com.petlogue.duopetbackend.pet.model.service;

import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import com.petlogue.duopetbackend.pet.model.dto.PetDto;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.common.FileNameChange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PetService {

    private final PetRepository petRepository;

    // 프로필 이미지 저장 경로
    private static final String UPLOAD_DIR = "C:/upload_files/pet";

    /**
     * 반려동물 등록 (회원가입 이후)
     */
    public void registerPet(PetDto petDto, UserEntity user) throws IOException {
        MultipartFile file = petDto.getFile();

        String originalFilename = null;
        String renameFilename = null;

        // 1. 파일이 있다면 처리
        if (file != null && !file.isEmpty()) {
            originalFilename = file.getOriginalFilename();
            String timestamp = FileNameChange.change(originalFilename, "yyyyMMddHHmmss");
            renameFilename = "pet_" + timestamp;

            File uploadPath = new File(UPLOAD_DIR, renameFilename);
            if (!uploadPath.getParentFile().exists()) {
                uploadPath.getParentFile().mkdirs();
            }

            file.transferTo(uploadPath);
        }

        // 2. DTO → Entity 변환
        PetEntity pet = petDto.toEntity(user);
        pet.setRegistrationDate(LocalDate.now());
        pet.setOriginalFilename(originalFilename);
        pet.setRenameFilename(renameFilename);

        // 3. DB 저장
        petRepository.save(pet);
    }
    
    /**
     * 사용자의 반려동물 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PetDto> getUserPets(Long userId) {
        List<PetEntity> pets = petRepository.findByUser_UserId(userId);
        return pets.stream()
                .map(PetDto::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 특정 반려동물 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public PetDto getPetDetail(Long petId) {
        return petRepository.findById(petId)
                .map(PetDto::fromEntity)
                .orElse(null);
    }
    
    /**
     * 반려동물 정보 수정
     */
    public void updatePet(Long petId, PetDto petDto) throws IOException {
        PetEntity pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("반려동물을 찾을 수 없습니다."));
        
        // 기본 정보 업데이트
        pet.setPetName(petDto.getPetName());
        pet.setAnimalType(petDto.getAnimalType());
        pet.setBreed(petDto.getBreed());
        pet.setAge(petDto.getAge());
        pet.setGender(petDto.getGender());
        pet.setNeutered(petDto.getNeutered());
        pet.setWeight(petDto.getWeight());
        
        // 파일이 있다면 처리
        MultipartFile file = petDto.getFile();
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제
            if (pet.getRenameFilename() != null) {
                File oldFile = new File(UPLOAD_DIR, pet.getRenameFilename());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            
            // 새 파일 저장
            String originalFilename = file.getOriginalFilename();
            String timestamp = FileNameChange.change(originalFilename, "yyyyMMddHHmmss");
            String renameFilename = "pet_" + timestamp;
            
            File uploadPath = new File(UPLOAD_DIR, renameFilename);
            if (!uploadPath.getParentFile().exists()) {
                uploadPath.getParentFile().mkdirs();
            }
            
            file.transferTo(uploadPath);
            
            pet.setOriginalFilename(originalFilename);
            pet.setRenameFilename(renameFilename);
        }
        
        petRepository.save(pet);
    }
    
    /**
     * 반려동물 삭제
     */
    public void deletePet(Long petId) {
        PetEntity pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("반려동물을 찾을 수 없습니다."));
        
        // 파일 삭제
        if (pet.getRenameFilename() != null) {
            File file = new File(UPLOAD_DIR, pet.getRenameFilename());
            if (file.exists()) {
                file.delete();
            }
        }
        
        petRepository.delete(pet);
    }
}
