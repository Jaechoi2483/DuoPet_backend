package com.petlogue.duopetbackend.board.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.ReportEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report {

    private Long reportId;         // 신고 고유 ID
    private Long userId;           // 신고한 사용자 ID
    private Long targetId;         // 신고 대상 ID
    private String targetType;     // 신고 대상 유형 (content, comment, review)
    private String reason;         // 신고 사유
    private String details;        // 신고 상세 설명
    private String status;         // 신고 상태 (PENDING, REVIEWED, BLOCKED)
    private Date createdAt;        // 신고 접수 시간

    // 신고관리용
    private String userLoginId;    // 신고자 로그인 ID
    private String targetLoginId;   // 신고대상자 로그인 ID

    private LocalDateTime suspendedUntil; // 정지 만료 시간

    public ReportEntity toReportEntity(UserEntity user) {
        return ReportEntity.builder()
                .user(user)
                .targetId(targetId)
                .targetType(targetType)
                .reason(reason)
                .details(details)
                .status("PENDING") // 기본값
                .createdAt(new Date()) // 현재 시간을 Date로 설정
                .build();
    }
}
