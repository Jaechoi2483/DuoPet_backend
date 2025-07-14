package com.petlogue.duopetbackend.user.model.service;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.user.jpa.entity.ShelterEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.ShelterRepository;
import com.petlogue.duopetbackend.user.model.dto.ShelterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
@Transactional
@RequiredArgsConstructor
public class ShelterService {

    private final ShelterRepository shelterRepository;

    /**
     * [회원가입 3단계]
     * - 인증파일을 임시 디렉토리에 저장 (DB 저장은 하지 않음)
     * - 경로: C:/upload_files/shelter/temp
     * - 리턴: ShelterDto에 파일명 정보 세팅
     */
    public ShelterDto processShelterFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("인증 첨부파일은 필수입니다.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String renameFilename = FileNameChange.change(originalFilename, "yyyyMMddHHmmss");

            String uploadDir = "C:/upload_files/shelter/temp";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, renameFilename);
            file.transferTo(dest);

            ShelterDto dto = new ShelterDto();
            dto.setShelterFileOriginalFilename(originalFilename);
            dto.setShelterFileRenameFilename(renameFilename);
            return dto;

        } catch (Exception e) {
            throw new RuntimeException("인증 파일 임시 저장 중 오류 발생", e);
        }
    }

    /**
     * [회원가입 최종 단계]
     * - 보호소 등록 처리
     * - 임시파일 → 실제 디렉토리 이동 (C:/upload_files/shelter)
     * - ShelterEntity 저장
     */
    public void registerShelter(UserEntity userEntity, ShelterDto shelterDto) {
        // 1. 중복 체크
        if (shelterRepository.existsByUser(userEntity)) {
            throw new IllegalStateException("이미 보호소로 등록된 사용자입니다.");
        }

        // 2. 파일 이동
        String tempDirPath = "C:/upload_files/shelter/temp";
        String realDirPath = "C:/upload_files/shelter";

        File tempFile = new File(tempDirPath, shelterDto.getShelterFileRenameFilename());
        File realFile = new File(realDirPath, shelterDto.getShelterFileRenameFilename());

        if (!tempFile.exists()) {
            throw new RuntimeException("임시 파일이 존재하지 않습니다: " + tempFile.getPath());
        }

        try {
            File realDir = new File(realDirPath);
            if (!realDir.exists()) realDir.mkdirs();

            boolean success = tempFile.renameTo(realFile);
            if (!success) {
                throw new RuntimeException("인증 파일 이동 실패");
            }

        } catch (Exception e) {
            throw new RuntimeException("파일 이동 중 오류 발생", e);
        }

        // 3. Entity 저장
        ShelterEntity shelter = shelterDto.toEntity(userEntity);
        shelterRepository.save(shelter);
    }
}
