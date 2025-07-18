package com.petlogue.duopetbackend.admin.model.service;


import com.petlogue.duopetbackend.admin.model.dto.DashboardDataDto;
import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDetailDto;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j; // 💡 @RequiredArgsConstructor를 사용하지 않으므로 lombok.RequiredArgsConstructor는 삭제해도 됩니다.
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AdminService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final String aiApiKey;
    private final String aiServerUrl;

    @Value("${file.upload-dir}")
    private String baseUploadPath;

    // ▼▼▼ 1. @RequiredArgsConstructor 삭제 후, 수동으로 생성자 작성 ▼▼▼
    public AdminService(
            PetRepository petRepository,
            UserRepository userRepository,
            @Value("${duopet.ai.api-key}") String aiApiKey,
            @Value("${duopet.ai.server-url}") String aiServerUrl
    ) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.aiApiKey = aiApiKey;
        this.aiServerUrl = aiServerUrl;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAllUsers(Pageable pageable, String role, String status) {
        Page<UserEntity> userPage;

        boolean hasRole = role != null && !role.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasRole && hasStatus) {
            userPage = userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (hasRole) {
            userPage = userRepository.findByRole(role, pageable);
        } else if (hasStatus) {
            userPage = userRepository.findByStatus(status, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return userPage.map(UserEntity::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto findUserDetailById(Long userId) {
        UserDetailDto result = userRepository.findDetailWithProfiles(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        UserEntity userEntity = result.getUser();
        UserDto userDto = userEntity.toDto();

        if (result.getVetProfile() != null) {
            userDto.setVetProfile(result.getVetProfile().toDto());
        }
        if (result.getShelterProfile() != null) {
            userDto.setShelterProfile(result.getShelterProfile().toDto());
        }

        return userDto;
    }

    public Resource loadVetFile(String filename) {
        try {
            Path filePath = Paths.get(baseUploadPath, "vet").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 올바르지 않습니다: " + filename, e);
        }
    }

    public Resource loadShelterFile(String filename) {
        try {
            Path filePath = Paths.get(baseUploadPath, "shelter").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 올바르지 않습니다: " + filename, e);
        }
    }

    public void updateUserRole(Long userId, String newRole) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));
        user.setRole(newRole);
    }

    public void updateUserStatus(Long userId, String newStatus) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));
        user.setStatus(newStatus);
    }

    public DashboardDataDto getDashboardData() {
        long totalUserCount = userRepository.count();
        long totalPetCount = petRepository.count();
        long vetUserCount = userRepository.countByRole("vet");
        long shelterUserCount = userRepository.countByRole("shelter");

        List<StatItemDto> summaryList = List.of(
                new StatItemDto("총 회원 수", totalUserCount),
                new StatItemDto("총 반려동물 수", totalPetCount),
                new StatItemDto("수의사 회원", vetUserCount),
                new StatItemDto("보호소 회원", shelterUserCount)
        );

        List<StatItemDto> genderStat = userRepository.findGenderStat();
        List<StatItemDto> petCountStat = userRepository.findPetCountStat();
        List<StatItemDto> animalTypeStat = petRepository.findAnimalTypeStat();
        List<StatItemDto> neuteredStat = petRepository.findNeuteredStat();

        return DashboardDataDto.builder()
                .summary(summaryList)
                .genderStat(genderStat)
                .petCountStat(petCountStat)
                .animalTypeStat(animalTypeStat)
                .neuteredStat(neuteredStat)
                .build();
    }

    // ▼▼▼ 2. resyncChatbotData 메서드 수정 ▼▼▼
    public void resyncChatbotData() {
        log.info("챗봇 데이터 동기화 서비스 로직 시작");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        // 클래스 필드에 주입된 aiApiKey 사용
        headers.set("X-API-KEY", this.aiApiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        try {
            // 클래스 필드에 주입된 aiServerUrl 사용
            // requestEntity를 전송하도록 수정
            ResponseEntity<String> response = restTemplate.postForEntity(this.aiServerUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("AI 서버로부터 동기화 성공 응답 받음: {}", response.getBody());
            } else {
                log.error("AI 서버 동기화 실패. 응답 코드: {}", response.getStatusCode());
                throw new RuntimeException("AI 서버 동기화에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("AI 서버 통신 중 오류 발생", e);
            throw new RuntimeException("AI 서버와 통신할 수 없습니다.", e);
        }
    }
}