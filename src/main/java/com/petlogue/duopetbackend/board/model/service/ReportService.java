package com.petlogue.duopetbackend.board.model.service;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import com.petlogue.duopetbackend.board.jpa.entity.CommentsEntity;
import com.petlogue.duopetbackend.board.jpa.entity.ReportEntity;
import com.petlogue.duopetbackend.board.jpa.repository.BoardRepository;
import com.petlogue.duopetbackend.board.jpa.repository.CommentsRepository;
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
    private final CommentsRepository commentsRepository;

    public void saveReport(Long userId, Report dto) {

        // 1. 중복 신고 체크
        boolean exists = reportRepository.existsByUser_UserIdAndTargetIdAndTargetType(
                userId, dto.getTargetId(), dto.getTargetType());

        if (exists) {
            throw new IllegalStateException("이미 신고한 항목입니다.");
        }

        // 2. 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효한 사용자 ID가 아닙니다."));

        // 3. 본인 신고 차단
        switch (dto.getTargetType().toLowerCase()) {
            case "content" -> {
                BoardEntity board = boardRepository.findById(dto.getTargetId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

                if (board.getUserId().equals(userId)) {
                    throw new IllegalArgumentException("자신의 게시글은 신고할 수 없습니다.");
                }
            }
            case "comment" -> {
                CommentsEntity comment = commentsRepository.findById(dto.getTargetId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

                if (comment.getUser().getUserId().equals(userId)) {
                    throw new IllegalArgumentException("자신의 댓글은 신고할 수 없습니다.");
                }
            }
            // 필요한 경우 "review", "faq" 등 다른 신고 대상도 이 구조에 추가 가능
        }

        // 4. 신고 엔티티 저장
        ReportEntity report = dto.toReportEntity(user);
        reportRepository.save(report);

        log.info("신고 저장 완료: {}", report);
    }
}
