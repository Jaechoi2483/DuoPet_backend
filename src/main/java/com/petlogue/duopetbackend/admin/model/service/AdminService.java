package com.petlogue.duopetbackend.admin.model.service;


import com.petlogue.duopetbackend.admin.model.dto.DashboardDataDto;
import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDetailDto;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String baseUploadPath;

    @Transactional(readOnly = true)
    public Page<UserDto> findAllUsers(Pageable pageable, String role, String status) {
        Page<UserEntity> userPage;

        // 파라미터 존재 여부 확인
        boolean hasRole = role != null && !role.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasRole && hasStatus) {
            // 1. Role과 Status 모두 있는 경우
            userPage = userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (hasRole) {
            // 2. Role만 있는 경우
            userPage = userRepository.findByRole(role, pageable);
        } else if (hasStatus) {
            // 3. Status만 있는 경우
            userPage = userRepository.findByStatus(status, pageable);
        } else {
            // 4. 필터 조건이 모두 없는 경우
            userPage = userRepository.findAll(pageable);
        }

        // 조회된 Page<Entity>를 Page<Dto>로 변환하여 반환
        return userPage.map(UserEntity::toDto);

    }
    @Transactional(readOnly = true)
    public UserDto findUserDetailById(Long userId) {
        // 1. Repository의 JOIN 쿼리를 호출하여 UserDetailDto 타입으로 결과를 한 번에 받습니다.
        UserDetailDto result = userRepository.findDetailWithProfiles(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        // 2. 결과 객체에서 각 엔티티를 꺼내 최종 UserDto를 조립합니다.
        UserEntity userEntity = result.getUser();
        UserDto userDto = userEntity.toDto(); // 기본 정보 변환

        // 3. 역할에 따라 프로필 정보를 DTO에 추가합니다.
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
            // 기본 경로와 하위 폴더(vet)를 조합하여 전체 경로 생성
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
            // 기본 경로와 하위 폴더(shelter)를 조합하여 전체 경로 생성
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
        // 1. 사용자 엔티티를 조회합니다.
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));


        user.setRole(newRole);

        // 3. @Transactional에 의해 메서드 종료 시 변경된 내용이 자동으로 DB에 반영됩니다.
    }
    public void updateUserStatus(Long userId, String newStatus) {
        // 1. 사용자 엔티티를 조회합니다.
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        // 2. 상태(Status)를 업데이트합니다.
        // status 필드도 String 타입이므로, 변환 없이 바로 값을 설정합니다.
        user.setStatus(newStatus);

        // 3. @Transactional에 의해 메서드 종료 시 변경된 내용이 자동으로 DB에 반영됩니다.
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


        // 성별 통계 데이터 조회
        List<StatItemDto> genderStat = userRepository.findGenderStat();

        // 반려동물 보유 수 통계 데이터 조회
        List<StatItemDto> petCountStat = userRepository.findPetCountStat();

        // 반려동물 종류 통계
        List<StatItemDto> animalTypeStat = petRepository.findAnimalTypeStat();
// 중성화 비율
        List<StatItemDto> neuteredStat = petRepository.findNeuteredStat();

        // 최종 응답 DTO 조립
        return DashboardDataDto.builder()
                .summary(summaryList)
                .genderStat(genderStat)
                .petCountStat(petCountStat)
                .animalTypeStat(animalTypeStat)
                .neuteredStat(neuteredStat)
                .build();
    }
}
