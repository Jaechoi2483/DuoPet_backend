package com.petlogue.duopetbackend.board.jpa.entity;

import com.petlogue.duopetbackend.board.model.dto.Report;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "REPORT")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(length = 30)
    private String reason;

    @Column(nullable = false)
    private String status = "PENDING"; // 기본값

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(length = 1000)
    private String details;  // 상세 설명 (선택사항)

    public Report toReportDto() {
        return Report.builder()
                .reportId(this.reportId)
                .userId(this.user.getUserId()) // userId는 UserEntity에서 가져옴
                .targetId(this.targetId)
                .targetType(this.targetType)
                .reason(this.reason)
                .status(this.status)
                .createdAt(this.createdAt)
                .details(this.details)
                .build();
    }
}
