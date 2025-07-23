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

        // 모든 대상 ID와 타입을 한 번에 수집
        Map<String, List<Long>> targetIdsByType = reportEntities.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getTargetType().toLowerCase(), // "content" 또는 "comment"
                        Collectors.mapping(ReportEntity::getTargetId, Collectors.toList())
                ));


        Map<Long, String> boardStatusMap = new HashMap<>();
        if (targetIdsByType.containsKey("content")) {
            List<Long> contentIds = targetIdsByType.get("content");
            // ⭐ 이 부분이 핵심 수정입니다. ⭐
            // List<Long>을 Iterable<Long>으로 직접 전달합니다.
            // JpaRepository의 findAllById는 Iterable<ID>를 받으므로, 여기서 ID가 Long으로 추론됩니다.
            List<BoardEntity> boards = boardRepository.findAllById(contentIds); // 명시적 캐스팅 제거, 그대로 전달
            boards.forEach(board ->
                    boardStatusMap.put(board.getContentId(), board.getStatus())
            );
        }

        Map<Long, String> commentStatusMap = new HashMap<>();
        if (targetIdsByType.containsKey("comment")) {
            List<Long> commentIds = targetIdsByType.get("comment");
            // ⭐ 이 부분이 핵심 수정입니다. ⭐
            List<CommentsEntity> comments = commentsRepository.findAllById(commentIds); // 명시적 캐스팅 제거, 그대로 전달
            comments.forEach(comment ->
                    commentStatusMap.put(comment.getCommentId(), comment.getStatus())
            );
        }


        // 모든 관련 사용자 ID를 수집하여 한 번에 조회
        Set<Long> allUserIds = new HashSet<>();
        reportEntities.forEach(report -> allUserIds.add(report.getUser().getUserId())); // 신고자 ID

        // findReportedUser 로직에서 필요한 ReportedUser ID 수집
        for (ReportEntity report : reportEntities) {
            String targetType = report.getTargetType();
            Long targetId = report.getTargetId();

            if ("content".equalsIgnoreCase(targetType)) {
                boardRepository.findById(targetId)
                        .map(BoardEntity::getUserId)
                        .ifPresent(allUserIds::add);
            } else if ("comment".equalsIgnoreCase(targetType)) {
                commentsRepository.findById(targetId)
                        .map(CommentsEntity::getUser)
                        .map(UserEntity::getUserId)
                        .ifPresent(allUserIds::add);
            }
        }

        Map<Long, UserEntity> userMap = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, user -> user));


        return reportEntities.stream()
                .map(entity -> {
                    UserEntity reporter = userMap.get(entity.getUser().getUserId());
                    UserEntity reportedUser = null;
                    String targetContentStatus = null; // ⭐ 이 변수에 값을 할당해야 합니다. ⭐
                    Long reportedUserId = null;


                    if ("content".equalsIgnoreCase(entity.getTargetType())) {
                        reportedUserId = boardRepository.findById(entity.getTargetId()).map(BoardEntity::getUserId).orElse(null);
                        // ⭐ BoardEntity에서 상태를 가져와 targetContentStatus에 할당 ⭐
                        targetContentStatus = boardStatusMap.get(entity.getTargetId());
                    } else if ("comment".equalsIgnoreCase(entity.getTargetType())) {
                        CommentsEntity comment = commentsRepository.findById(entity.getTargetId()).orElse(null);
                        if (comment != null) {
                            reportedUserId = comment.getUser().getUserId();
                            // ⭐ CommentsEntity에서 상태를 가져와 targetContentStatus에 할당 ⭐
                            targetContentStatus = commentStatusMap.get(entity.getTargetId());
                        }
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
                            .createdAt(entity.getCreatedAt()) // ReportEntity의 createdAt (Date 타입)
                            .details(entity.getDetails())

                            .targetContentStatus(targetContentStatus != null ? targetContentStatus : "UNKNOWN")
                            .build();
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public Report updateReportStatus(Long reportId, String action) {
        log.info(">>>>> Report Status Update. reportId: {}, action: {}", reportId, action);
        ReportEntity reportEntity = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 신고를 찾을 수 없습니다: " + reportId));

        switch (action) {
            case "REVIEWED":
                reportEntity.setStatus("REVIEWED"); // 리포트 상태를 '보류'로 변경
                log.info(">>>>> Report ID {}의 상태를 'REVIEWED'(으)로 변경했습니다.", reportId);
                break;

            case "PROCESSED": // ⭐ 프론트에서 넘어오는 'PROCESSED'를 블라인드 액션으로 처리 ⭐
                log.info(">>>>> Report ID {}에 대한 블라인드 처리 (콘텐츠 상태 INACTIVE)", reportId);

                if ("content".equalsIgnoreCase(reportEntity.getTargetType())) {
                    BoardEntity board = boardRepository.findById(reportEntity.getTargetId())
                            .orElseThrow(() -> new IllegalArgumentException("대상 게시글을 찾을 수 없습니다."));
                    board.setStatus("INACTIVE"); // 게시글 상태를 INACTIVE로 변경
                    boardRepository.save(board); // 변경된 게시글 상태 저장
                    log.info("게시글 ID {} 블라인드 처리 완료 (상태 INACTIVE)", reportEntity.getTargetId());
                } else if ("comment".equalsIgnoreCase(reportEntity.getTargetType())) {
                    CommentsEntity comment = commentsRepository.findById(reportEntity.getTargetId())
                            .orElseThrow(() -> new IllegalArgumentException("대상 댓글을 찾을 수 없습니다."));
                    comment.setStatus("INACTIVE"); // 댓글 상태를 INACTIVE로 변경
                    commentsRepository.save(comment); // 변경된 댓글 상태 저장
                    log.info("댓글 ID {} 블라인드 처리 완료 (상태 INACTIVE)", reportEntity.getTargetId());
                } else {
                    log.warn("알 수 없는 타겟 타입 '{}'에 대한 블라인드 처리. 콘텐츠 상태 변경 없음.", reportEntity.getTargetType());
                }
                // ⭐ 중요: reportEntity.setStatus("PROCESSED"); 이 라인을 제거합니다. ⭐
                // ReportEntity의 status는 'BLOCKED' 등 다른 액션에서만 변경됩니다.
                break; // ⭐ break 추가 ⭐

            case "BLOCK_3DAYS":
            case "BLOCK_7DAYS":
            case "BLOCK_1MONTH":
            case "BLOCK_PERMANENT":
                Long targetUserId = findTargetUserId(reportEntity);
                UserEntity targetUser = (targetUserId != null) ? userRepository.findById(targetUserId).orElse(null) : null;

                if (targetUser == null) {
                    log.warn("신고 대상 사용자(ID: {})를 찾을 수 없어 정지 처리를 할 수 없습니다.", targetUserId);
                    return reportEntity.toReportDto();
                }

                if ("BLOCK_3DAYS".equals(action)) targetUser.setSuspendedUntil(LocalDateTime.now().plusDays(3));
                else if ("BLOCK_7DAYS".equals(action)) targetUser.setSuspendedUntil(LocalDateTime.now().plusDays(7));
                else if ("BLOCK_1MONTH".equals(action)) targetUser.setSuspendedUntil(LocalDateTime.now().plusMonths(1));
                else if ("BLOCK_PERMANENT".equals(action)) targetUser.setSuspendedUntil(null);

                targetUser.setStatus("suspended"); // 사용자 상태를 'suspended'로 변경
                userRepository.save(targetUser); // 사용자 상태 저장

                reportEntity.setStatus("BLOCKED"); // ⭐ ReportEntity 상태를 'BLOCKED'로 변경 ⭐
                log.info(">>>>> Report ID {}의 상태를 'BLOCKED'(으)로 변경했습니다.", reportId);
                break;

            default:
                throw new IllegalArgumentException("알 수 없는 액션 값입니다: " + action);
        }

        reportRepository.save(reportEntity); // ReportEntity 변경 사항 저장 (status가 변경되지 않았다면 변경 없음)
        return reportEntity.toReportDto(); // 업데이트된 Report DTO 반환
    }

    // 사용자 ID를 찾는 중복 로직 (헬퍼 메서드)
    private Long findTargetUserId(ReportEntity reportEntity) {
        String targetType = reportEntity.getTargetType();
        Long targetId = reportEntity.getTargetId();

        if ("content".equalsIgnoreCase(targetType)) {
            return boardRepository.findById(targetId).map(BoardEntity::getUserId).orElse(null);
        } else if ("comment".equalsIgnoreCase(targetType)) {
            return commentsRepository.findById(targetId)
                    .map(CommentsEntity::getUser)
                    .map(UserEntity::getUserId)
                    .orElse(null);
        }
        return null;
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
        return reportEntity.getTargetId() + ":" + reportEntity.getTargetType();
    }

}