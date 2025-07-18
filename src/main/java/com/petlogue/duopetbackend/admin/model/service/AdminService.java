package com.petlogue.duopetbackend.admin.model.service;


import com.petlogue.duopetbackend.admin.model.dto.DashboardDataDto;
import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.entity.ReportEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
import com.petlogue.duopetbackend.board.jpa.repository.ReportRepository;
import com.petlogue.duopetbackend.board.model.dto.Report;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDetailDto;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional

public class AdminService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final CommentsRepository commentsRepository;
    private final BoardRepository boardRepository;
    private final String aiApiKey;
    private final String aiServerUrl;

    @Value("${file.upload-dir}")
    private String baseUploadPath;

    // 2. 아래 생성자 코드를 직접 작성합니다.
    public AdminService(
            PetRepository petRepository,
            UserRepository userRepository,
            ReportRepository reportRepository,
            CommentsRepository commentsRepository,
            BoardRepository boardRepository,
            @Value("${duopet.ai.api-key}") String aiApiKey, // @Value를 파라미터에 직접 적용
            @Value("${duopet.ai.server-url}") String aiServerUrl
    ) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.commentsRepository = commentsRepository;
        this.boardRepository = boardRepository;
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
    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        List<ReportEntity> reportEntities = reportRepository.findAllByOrderByCreatedAtDesc();

        // --- 신고 대상 콘텐츠 존재 여부 일괄 확인 (성능 최적화) ---
        // 1. 신고 대상을 타입(content, comment)별로 ID를 그룹화합니다.
        List<Long> contentIds = reportEntities.stream()
                .filter(r -> "content".equalsIgnoreCase(r.getTargetType()))
                .map(ReportEntity::getTargetId).distinct().toList();
        List<Long> commentIds = reportEntities.stream()
                .filter(r -> "comment".equalsIgnoreCase(r.getTargetType()))
                .map(ReportEntity::getTargetId).distinct().toList();
        List<Number> numberContentIds = new ArrayList<>(contentIds);
        Set<Long> existingContentIds = boardRepository.findAllById(numberContentIds).stream()
                .map(BoardEntity::getContentId).collect(Collectors.toSet());

        // [수정] CommentsRepository는 Long 타입을 사용하므로, 변환 없이 commentIds를 바로 사용합니다.
        Set<Long> existingCommentIds = commentsRepository.findAllById(commentIds).stream()
                .map(CommentsEntity::getCommentId).collect(Collectors.toSet());
        // -----------------------------------------------------------------

        // --- 기존 로직 (사용자 정보 조회) ---
        List<Long> targetUserIds = reportEntities.stream().map(ReportEntity::getTargetId).distinct().toList();
        Map<Long, UserEntity> targetUserMap = userRepository.findAllById(targetUserIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, user -> user));

        // --- 최종 DTO 생성 ---
        return reportEntities.stream()
                .map(entity -> {
                    String finalStatus = entity.getStatus(); // 기본 상태는 DB 값

                    // [신규 로직] 상태가 PENDING일 때만 존재 여부 확인
                    if ("PENDING".equals(entity.getStatus())) {
                        boolean isDeleted = false;
                        if ("content".equalsIgnoreCase(entity.getTargetType())) {
                            if (!existingContentIds.contains(entity.getTargetId())) {
                                isDeleted = true;
                            }
                        } else if ("comment".equalsIgnoreCase(entity.getTargetType())) {
                            if (!existingCommentIds.contains(entity.getTargetId())) {
                                isDeleted = true;
                            }
                        }
                        if (isDeleted) {
                            finalStatus = "DELETED_CONTENT"; // 존재하지 않으면 DTO의 상태를 변경
                        }
                    }

                    // ... (기존 사용자 정보 매핑 로직) ...
                    UserEntity targetUser = targetUserMap.get(entity.getTargetId());
                    String targetLoginId = (targetUser != null) ? targetUser.getLoginId() : "알 수 없음";
                    LocalDateTime suspendedUntil = (targetUser != null) ? targetUser.getSuspendedUntil() : null;

                    return Report.builder()
                            .reportId(entity.getReportId())
                            .userId(entity.getUser().getUserId())
                            .targetId(entity.getTargetId())
                            .targetType(entity.getTargetType())
                            .reason(entity.getReason())
                            .status(finalStatus) // <-- 최종 결정된 상태를 사용
                            .createdAt(entity.getCreatedAt())
                            .details(entity.getDetails())
                            .userLoginId(entity.getUser().getLoginId())
                            .targetLoginId(targetLoginId)
                            .suspendedUntil(suspendedUntil)
                            .build();
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public Report updateReportStatus(Long reportId, String action) { // 변수명을 newStatus에서 action으로 변경하여 명확화
        ReportEntity reportEntity = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 신고를 찾을 수 없습니다: " + reportId));

        UserEntity targetUser = userRepository.findById(reportEntity.getTargetId())
                .orElse(null);

        // '삭제' 액션은 별도로 먼저 처리
        if ("DELETED".equals(action)) {
            String targetType = reportEntity.getTargetType();
            Long targetId = reportEntity.getTargetId();

            if ("content".equalsIgnoreCase(targetType)) {
                boardRepository.deleteById(targetId);
            } else if ("comment".equalsIgnoreCase(targetType)) {
                commentsRepository.deleteById(targetId);
            }

            reportEntity.setStatus("REVIEWED"); // 콘텐츠 삭제 후 신고 상태는 '처리완료'로 변경
            return reportRepository.save(reportEntity).toReportDto();
        }

        // 사용자 정지 관련 로직
        if (targetUser != null) {
            switch (action) {
                case "BLOCK_3DAYS":
                    targetUser.setStatus("suspended");
                    targetUser.setSuspendedUntil(LocalDateTime.now().plusDays(3));
                    reportEntity.setStatus("BLOCKED"); // [수정] 신고 상태를 DB 허용 값인 'BLOCKED'로 설정
                    break;
                case "BLOCK_7DAYS":
                    targetUser.setStatus("suspended");
                    targetUser.setSuspendedUntil(LocalDateTime.now().plusDays(7));
                    reportEntity.setStatus("BLOCKED"); // [수정] 신고 상태를 DB 허용 값인 'BLOCKED'로 설정
                    break;
                case "BLOCK_1MONTH":
                    targetUser.setStatus("suspended");
                    targetUser.setSuspendedUntil(LocalDateTime.now().plusMonths(1));
                    reportEntity.setStatus("BLOCKED"); // [수정] 신고 상태를 DB 허용 값인 'BLOCKED'로 설정
                    break;
                case "BLOCK_PERMANENT":
                    targetUser.setStatus("inactive");
                    targetUser.setSuspendedUntil(null);
                    reportEntity.setStatus("BLOCKED"); // [수정] 신고 상태를 DB 허용 값인 'BLOCKED'로 설정
                    break;
                case "REVIEWED":
                    reportEntity.setStatus("REVIEWED"); // [수정] 신고 상태를 DB 허용 값인 'REVIEWED'로 설정
                    break;
                default:
                    throw new IllegalArgumentException("알 수 없는 액션 값입니다: " + action);
            }
            userRepository.save(targetUser);
        } else if ("REVIEWED".equals(action)) {
            // 신고 대상 유저가 없더라도, 신고 상태는 변경 가능
            reportEntity.setStatus("REVIEWED");
        }

        ReportEntity updatedReportEntity = reportRepository.save(reportEntity);
        return updatedReportEntity.toReportDto();
    }

}