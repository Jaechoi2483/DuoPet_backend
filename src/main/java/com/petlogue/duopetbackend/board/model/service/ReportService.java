package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.ReportEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.ReportRepository;
import com.petlogue.duopetbackend.board.model.dto.Report;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public void saveReport(Long userId, Report dto) {

        // 1. 중복 신고 체크
        boolean exists = reportRepository.existsByUser_UserIdAndTargetIdAndTargetType(
                userId, dto.getTargetId(), dto.getTargetType());

        if (exists) {
            throw new IllegalStateException("이미 해당 게시글을 신고하셨습니다.");
        }

        // 2. 본인 게시글 신고 막기 (게시글일 경우만)
        if ("content".equalsIgnoreCase(dto.getTargetType())) {
            BoardEntity targetBoard = boardRepository.findById(dto.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

            log.info("📌 게시글 작성자 ID: {}", targetBoard.getUserId());
            log.info("📌 신고자 ID: {}", userId);

            if (targetBoard.getUserId().equals(userId)) {
                throw new IllegalArgumentException("자신의 게시글은 신고할 수 없습니다.");
            }
        }

        // 3. 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효한 사용자 ID가 아닙니다."));

        // 4. 저장
        ReportEntity report = dto.toReportEntity(user);
        reportRepository.save(report);
    }

    // 댓글 신고하기
    public String toggleCommentReport(Long userId, Long commentId, Report dto) {
        String targetType = "comment";

        boolean alreadyReported = reportRepository.existsByUser_UserIdAndTargetIdAndTargetType(userId, commentId, targetType);

        if (alreadyReported) {
            return "이미 신고한 댓글입니다.";
        }

        ReportEntity report = ReportEntity.builder()
                .user(userRepository.findById(userId).orElseThrow())
                .targetId(commentId)
                .targetType(targetType)
                .reason(dto.getReason())
                .details(dto.getDetails())
                .status("PENDING")
                .createdAt(new Date())
                .build();

        reportRepository.save(report);
        return "신고가 접수되었습니다.";
    }
}
