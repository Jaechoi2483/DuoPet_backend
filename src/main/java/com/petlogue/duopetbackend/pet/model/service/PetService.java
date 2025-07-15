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
}
