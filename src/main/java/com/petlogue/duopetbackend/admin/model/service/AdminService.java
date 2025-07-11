package com.petlogue.duopetbackend.admin.model.service;


import com.petlogue.duopetbackend.admin.model.dto.DashboardDataDto;
import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;

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
        // Repository에서 성별 통계 데이터를 조회
        List<StatItemDto> genderStat = userRepository.findGenderStat();

        // Builder를 사용하여 최종 응답 DTO를 조립
        return DashboardDataDto.builder()
                .genderStat(genderStat)
                .build();
    }
}
