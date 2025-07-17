package com.petlogue.duopetbackend.board.jpa.entity;

import com.petlogue.duopetbackend.board.model.dto.Bookmark;
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
@Table(name= "BOOKMARK")
public class BookmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKMARK_ID")
    private Long bookmarkId;         // 북마크 고유 식별자 (PK)

    @Column(name = "USER_ID", nullable = false)
    private Long userId;             // 북마크를 누른 사용자 ID

    @Column(name = "CONTENT_ID", nullable = false)  // contentId로 수정
    private Long contentId;          // 게시글 ID (contentId 사용)

    @Column(name = "TARGET_TYPE", nullable = false, length = 50)
    private String targetType;       // 게시판 유형 (자유, 후기, 질문 등)

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;          // 북마크 생성일시 (SYSDATE)

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();  // INSERT 시 자동으로 현재 시간 세팅
    }

    // 생성자 수정 (contentId 사용)
    public BookmarkEntity(Long contentId, Long userId, String targetType) {
        this.contentId = contentId;  // contentId로 변경
        this.userId = userId;
        this.targetType = targetType;
    }

    // DTO로 변환
    public Bookmark toDto() {
        return Bookmark.builder()
                .bookmarkId(this.bookmarkId)
                .userId(this.userId)
                .contentId(this.contentId) // contentId 사용
                .targetType(this.targetType)
                .createdAt(this.createdAt)
                .build();
    }
}
