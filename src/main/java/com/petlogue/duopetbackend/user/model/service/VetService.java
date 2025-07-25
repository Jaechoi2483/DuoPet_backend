package com.petlogue.duopetbackend.user.model.service;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetProfile;
import com.petlogue.duopetbackend.consultation.jpa.repository.VetProfileRepository;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.jpa.repository.VetRepository;
import com.petlogue.duopetbackend.user.model.dto.VetDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VetService {

    private final VetRepository vetRepository;
    private final UserRepository userRepository;
    private final VetProfileRepository vetProfileRepository;

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
        VetEntity savedVet = vetRepository.save(vet);
        
        // 4. VetProfile 자동 생성
        createDefaultVetProfile(savedVet);
        
        log.info("전문가 등록 완료 - userId: {}, vetId: {}", userEntity.getUserId(), savedVet.getVetId());
    }
    
    /**
     * 기본 VetProfile 생성
     * - 회원가입 시 자동으로 생성되는 기본 프로필
     */
    private void createDefaultVetProfile(VetEntity vet) {
        // 이미 프로필이 있는지 확인
        if (vetProfileRepository.existsByVet(vet)) {
            log.warn("VetProfile이 이미 존재합니다. vetId: {}", vet.getVetId());
            return;
        }
        
        VetProfile profile = VetProfile.builder()
                .vet(vet)
                .introduction("안녕하세요. " + vet.getName() + " 수의사입니다. 반려동물의 건강한 삶을 위해 최선을 다하겠습니다.")
                .consultationFee(new BigDecimal("30000")) // 기본 상담료 30,000원
                .isAvailable("Y") // 기본값: 상담 가능
                .isOnline("N") // 기본값: 오프라인
                .ratingAvg(BigDecimal.ZERO)
                .ratingCount(0)
                .consultationCount(0)
                .responseTimeAvg(0)
                .build();
                
        vetProfileRepository.save(profile);
        log.info("VetProfile 자동 생성 완료 - vetId: {}", vet.getVetId());
    }

    /**
     * 마이페이지에서 전문가 역할 정보 저장 (insert or update)
     */
    public void updateVetInfo(VetDto vetDto) throws IOException {
        // 1. 사용자 조회
        UserEntity user = userRepository.findById(vetDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 2. 기존 vet 정보 있는지 확인 (★ 핵심)
        VetEntity vet = vetRepository.findByUser_UserId(vetDto.getUserId())
                .orElse(null);

        if (vet == null) {
            // vetId 없음 → insert
            vet = VetEntity.builder().user(user).build();
        } else {
            // vetId 있음 → update
            log.info("✅ 기존 vetId: {}", vet.getVetId());
        }

        // 3. 파일 업로드 처리
        MultipartFile file = vetDto.getLicenseFile();
        if (file != null && !file.isEmpty()) {
            String uploadDir = "C:/upload_files/vet";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalFilename = file.getOriginalFilename();
            String renameFilename = FileNameChange.change(originalFilename, "yyyyMMddHHmmss");

            File saveFile = new File(uploadDir, renameFilename);
            file.transferTo(saveFile);

            vet.setOriginalFilename(originalFilename);
            vet.setRenameFilename(renameFilename);
        }

        // 4. 기타 정보 설정
        vet.setName(user.getUserName());
        vet.setLicenseNumber(vetDto.getLicenseNumber());
        vet.setPhone(user.getPhone());
        vet.setEmail(user.getUserEmail());
        vet.setAddress(vetDto.getAddress());
        vet.setWebsite(vetDto.getWebsite());
        vet.setSpecialization(vetDto.getSpecialization());

        // 5. 저장 (insert or update 판단은 Hibernate가 vetId로 결정함)
        vetRepository.save(vet);
    }




}
