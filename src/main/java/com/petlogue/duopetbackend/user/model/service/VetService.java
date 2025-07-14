package com.petlogue.duopetbackend.user.model.service;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import com.petlogue.duopetbackend.user.jpa.repository.VetRepository;
import com.petlogue.duopetbackend.user.model.dto.VetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
@RequiredArgsConstructor
public class VetService {

    private final VetRepository vetRepository;

    /**
     * [회원가입 3단계]
     * - 면허증 파일을 임시 디렉토리에 저장 (DB 저장은 하지 않음)
     * - 경로: C:/upload_files/vet/temp
     * - 리턴: VetDto에 파일명 정보 세팅
     */
    public VetDto processVetFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("면허증 첨부파일은 필수입니다.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String renameFilename = FileNameChange.change(originalFilename, "yyyyMMddHHmmss");

            String uploadDir = "C:/upload_files/vet/temp";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, renameFilename);
            file.transferTo(dest);

            VetDto dto = new VetDto();
            dto.setVetFileOriginalFilename(originalFilename);
            dto.setVetFileRenameFilename(renameFilename);
            return dto;

        } catch (Exception e) {
            throw new RuntimeException("면허증 임시 파일 저장 중 오류 발생", e);
        }
    }

    /**
     * [회원가입 완료 시]
     * - 전문가 등록 처리
     * - 실제 디렉토리로 파일 이동 (C:/upload_files/vet)
     * - VetEntity 저장
     */
    public void registerVet(UserEntity userEntity, VetDto vetDto) {
        // 1. 중복 등록 확인
        if (vetRepository.existsByUser(userEntity)) {
            throw new IllegalStateException("이미 전문가로 등록된 사용자입니다.");
        }

        // 2. 면허증 파일 이동
        String tempDirPath = "C:/upload_files/vet/temp";
        String realDirPath = "C:/upload_files/vet";

        File tempFile = new File(tempDirPath, vetDto.getVetFileRenameFilename());
        File realFile = new File(realDirPath, vetDto.getVetFileRenameFilename());

        if (!tempFile.exists()) {
            throw new RuntimeException("임시 파일이 존재하지 않습니다: " + tempFile.getPath());
        }

        try {
            File realDir = new File(realDirPath);
            if (!realDir.exists()) realDir.mkdirs();

            boolean success = tempFile.renameTo(realFile);
            if (!success) {
                throw new RuntimeException("면허증 파일 이동 실패");
            }

        } catch (Exception e) {
            throw new RuntimeException("면허증 파일 이동 중 오류 발생", e);
        }

        // 3. Entity 저장
        VetEntity vet = vetDto.toEntity(userEntity);
        vetRepository.save(vet);
    }
}
