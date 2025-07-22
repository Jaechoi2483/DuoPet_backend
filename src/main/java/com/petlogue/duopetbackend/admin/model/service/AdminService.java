package com.petlogue.duopetbackend.admin.model.service;


import com.petlogue.duopetbackend.admin.model.dto.DashboardDataDto;
import com.petlogue.duopetbackend.admin.model.dto.StatItemDto;
import com.petlogue.duopetbackend.admin.model.dto.UserReportCountDto;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
        if (reportEntities.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<Long>> targetIdsByType = reportEntities.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getTargetType().toLowerCase(),
                        Collectors.mapping(ReportEntity::getTargetId, Collectors.toList())
                ));

        Map<Long, Long> contentIdToUserIdMap = new HashMap<>();
        if (targetIdsByType.containsKey("content")) {
            List<Long> contentIds = targetIdsByType.get("content");
            // boardRepository는 Iterable<Number>를 요구하므로 변환 유지
            List<Number> numberContentIds = new ArrayList<>(contentIds);
            List<BoardEntity> boards = boardRepository.findAllById(numberContentIds);
            boards.forEach(board -> contentIdToUserIdMap.put(board.getContentId(), board.getUserId()));
        }

        Map<Long, Long> commentIdToUserIdMap = new HashMap<>();
        if (targetIdsByType.containsKey("comment")) {
            List<Long> commentIds = targetIdsByType.get("comment");
            List<CommentsEntity> comments = commentsRepository.findAllById(commentIds);

            // ✅ 수정된 코드
            comments.forEach(comment ->
                    commentIdToUserIdMap.put(
                            comment.getCommentId(),
                            comment.getUser().getUserId() // UserEntity에서 Long 타입의 ID를 추출
                    )
            );
        }

        Set<Long> allUserIds = new HashSet<>();
        reportEntities.forEach(report -> allUserIds.add(report.getUser().getUserId()));
        allUserIds.addAll(contentIdToUserIdMap.values());
        allUserIds.addAll(commentIdToUserIdMap.values());

        Map<Long, UserEntity> userMap = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, user -> user));

        return reportEntities.stream()
                .map(entity -> {
                    UserEntity reporter = userMap.get(entity.getUser().getUserId());
                    UserEntity reportedUser = null;
                    Long reportedUserId = null;

                    if ("content".equalsIgnoreCase(entity.getTargetType())) {
                        reportedUserId = contentIdToUserIdMap.get(entity.getTargetId());
                    } else if ("comment".equalsIgnoreCase(entity.getTargetType())) {
                        reportedUserId = commentIdToUserIdMap.get(entity.getTargetId());
                    }

                    if (reportedUserId != null) {
                        reportedUser = userMap.get(reportedUserId);
                    }

                    return Report.builder()
                            .reportId(entity.getReportId())
                            .userId(reporter != null ? reporter.getUserId() : null)
                            .userLoginId(reporter != null ? reporter.getLoginId() : "알 수 없음")
                            .targetId(entity.getTargetId())
                            .targetType(entity.getTargetType())
                            .targetLoginId(reportedUser != null ? reportedUser.getLoginId() : "알 수 없음")
                            .suspendedUntil(reportedUser != null ? reportedUser.getSuspendedUntil() : null)
                            .reason(entity.getReason())
                            .status(entity.getStatus())
                            .createdAt(entity.getCreatedAt())
                            .details(entity.getDetails())
                            .build();
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public Report updateReportStatus(Long reportId, String action) {
        ReportEntity reportEntity = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 신고를 찾을 수 없습니다: " + reportId));

        // [핵심] '삭제' 액션일 경우, 컨텐츠만 삭제하고 신고 레코드의 상태는 변경하지 않습니다.
        if ("DELETED".equals(action)) {
            String targetType = reportEntity.getTargetType();
            Long targetId = reportEntity.getTargetId();

            if ("content".equalsIgnoreCase(targetType)) {
                commentsRepository.deleteAllByContentId(targetId);
                boardRepository.deleteById(targetId);
            } else if ("comment".equalsIgnoreCase(targetType)) {
                commentsRepository.deleteById(targetId);
            }
            // 상태 변경 없이, 현재 상태 그대로의 DTO를 반환합니다.
            return reportEntity.toReportDto();
        }

        // --- 이하 사용자 정지 또는 보류 처리 로직 ---
        String targetType = reportEntity.getTargetType();
        Long targetId = reportEntity.getTargetId();
        Long targetUserId = null;

        if ("content".equalsIgnoreCase(targetType)) {
            targetUserId = boardRepository.findById(targetId).map(BoardEntity::getUserId).orElse(null);
        } else if ("comment".equalsIgnoreCase(targetType)) {
            // ✅ 수정된 코드
            targetUserId = commentsRepository.findById(targetId)
                    .map(CommentsEntity::getUser)       // 1. UserEntity 객체를 추출
                    .map(UserEntity::getUserId)         // 2. UserEntity에서 Long 타입 ID를 추출
                    .orElse(null);

        }

        // [핵심] 정지 또는 보류 액션에 대해서만 상태를 변경합니다.
        switch (action) {
            case "BLOCK_3DAYS":
            case "BLOCK_7DAYS":
            case "BLOCK_1MONTH":
            case "BLOCK_PERMANENT":
                UserEntity targetUser = (targetUserId != null) ? userRepository.findById(targetUserId).orElse(null) : null;
                if (targetUser == null) {
                    log.warn("신고 대상 사용자(ID: {})를 찾을 수 없어 정지 처리를 할 수 없습니다. Report ID: {}", targetUserId, reportId);
                    return reportEntity.toReportDto(); // 사용자를 못 찾으면 아무것도 안 함
                }

                if ("BLOCK_3DAYS".equals(action)) targetUser.setSuspendedUntil(LocalDateTime.now().plusDays(3));
                if ("BLOCK_7DAYS".equals(action)) targetUser.setSuspendedUntil(LocalDateTime.now().plusDays(7));
                if ("BLOCK_1MONTH".equals(action)) targetUser.setSuspendedUntil(LocalDateTime.now().plusMonths(1));
                if ("BLOCK_PERMANENT".equals(action)) targetUser.setSuspendedUntil(null);

                targetUser.setStatus("suspended");
                reportEntity.setStatus("BLOCKED"); // 신고 상태는 'BLOCKED'로 변경
                userRepository.save(targetUser);
                break;

            case "REVIEWED":
                reportEntity.setStatus("REVIEWED"); // 신고 상태는 'REVIEWED'로 변경
                break;

            default:
                throw new IllegalArgumentException("알 수 없는 액션 값입니다: " + action);
        }

        ReportEntity updatedReportEntity = reportRepository.save(reportEntity);
        return updatedReportEntity.toReportDto();
    }

    @Transactional
    public void unblockUserByReportId(Long reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));

        // 신고 대상자를 정확히 찾는 로직
        Long targetUserId = null;
        String targetType = report.getTargetType();
        Long contentOrCommentId = report.getTargetId();

        if ("content".equalsIgnoreCase(targetType)) {
            targetUserId = boardRepository.findById(contentOrCommentId)
                    .map(BoardEntity::getUserId)
                    .orElse(null);
        } else if ("comment".equalsIgnoreCase(targetType)) {
            targetUserId = commentsRepository.findById(contentOrCommentId)
                    .map(CommentsEntity::getUser)       // Optional<UserEntity>
                    .map(UserEntity::getUserId)         // Optional<Long>
                    .orElse(null);
        }

        if (targetUserId == null) {
            throw new IllegalArgumentException("정지 해제 대상 사용자를 찾을 수 없습니다. Report ID: " + reportId);
        }

        // [수정] 람다에서 사용하기 위해 final 변수에 값을 복사합니다.
        final Long finalTargetUserId = targetUserId;
        UserEntity targetUser = userRepository.findById(finalTargetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found with ID: " + finalTargetUserId));

        targetUser.setStatus("active");
        targetUser.setSuspendedUntil(null);
        userRepository.save(targetUser);

        if ("BLOCKED".equals(report.getStatus())) {
            report.setStatus("REVIEWED");
            reportRepository.save(report);
        }
    }

    public List<UserReportCountDto> getAggregatedReportCounts() {
        // 1. DB에서 모든 ReportEntity를 조회합니다.
        List<ReportEntity> allReports = reportRepository.findAll();

        // 2. 신고 엔티티를 '신고당한 사용자(UserEntity)'를 기준으로 그룹화합니다.
        //    - findReportedUser 헬퍼 메서드를 사용해 신고 대상자를 찾습니다.
        //    - 신고 대상자를 찾을 수 없는 경우(탈퇴, 컨텐츠 삭제 등)는 집계에서 제외합니다.
        Map<UserEntity, Set<String>> reportsByReportedUser = allReports.stream()
                .filter(report -> findReportedUser(report) != null) // 신고 대상자가 있는 경우만 필터링
                .collect(Collectors.groupingBy(
                        this::findReportedUser, // 신고당한 사용자를 기준으로 그룹핑
                        Collectors.mapping(
                                this::generateUniqueReportKey, // 각 신고를 고유 식별자로 변환
                                Collectors.toSet()             // Set으로 중복 제거
                        )
                ));

        // 3. 그룹화된 데이터를 UserReportCountDto 리스트로 변환합니다.
        return reportsByReportedUser.entrySet().stream()
                .map(entry -> new UserReportCountDto(
                        entry.getKey().getUserId(),
                        entry.getKey().getLoginId(),      // UserEntity에서 로그인 ID 가져오기
                        entry.getValue().size()           // Set의 크기가 누적 신고 횟수
                ))
                .collect(Collectors.toList());
    }

    private UserEntity findReportedUser(ReportEntity reportEntity) {
        String targetType = reportEntity.getTargetType();
        Long targetId = reportEntity.getTargetId(); // 신고된 게시물 ID 또는 댓글 ID

        if ("content".equalsIgnoreCase(targetType)) {
            // 1. 게시물 ID로 BoardEntity를 찾고,
            // 2. 찾은 게시물의 userId로 UserRepository에서 UserEntity를 찾습니다.
            return boardRepository.findById(targetId)
                    .flatMap(board -> userRepository.findById(board.getUserId())) // [수정] flatMap을 사용해 연계 조회
                    .orElse(null); // 최종적으로 사용자를 찾지 못하면 null 반환
        } else if ("comment".equalsIgnoreCase(targetType)) {
            return commentsRepository.findById(targetId)
                    // UserEntity 객체에서 Long 타입의 ID를 가져와 전달합니다.
                    .flatMap(comment -> userRepository.findById(comment.getUser().getUserId()))
                    .orElse(null);
        }

        return null;
    }


    private String generateUniqueReportKey(ReportEntity reportEntity) {
        // java.util.Date를 LocalDate로 변환합니다.
        LocalDate reportDate = reportEntity.getCreatedAt().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        return reportEntity.getTargetId() + ":" + reportEntity.getTargetType() + ":" + reportDate.toString();
    }

}