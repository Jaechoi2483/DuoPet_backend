package com.petlogue.duopetbackend.board.jpa.entity;

import com.petlogue.duopetbackend.board.model.dto.Board;
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
@Table(name= "CONTENT")
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONTENT_ID")
    private Long contentId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "TITLE", length = 1000, nullable = false)
    private String title;

    @Lob
    @Column(name = "CONTENT_BODY", nullable = false)
    private String contentBody;

    @Column(name = "CONTENT_TYPE", length = 50, nullable = false)
    private String contentType;  // 예: "board", "notice"

    @Column(name = "CATEGORY", length = 200)
    private String category;     // 예: "free", "review", "tip"

    @Column(name = "TAGS", length = 100)
    private String tags;

    @Column(name = "VIEW_COUNT")
    @Builder.Default
    private Integer  viewCount = 0;

    @Column(name = "LIKE_COUNT")
    @Builder.Default
    private Integer  likeCount = 0;

    @Column(name = "BOOKMARK_COUNT", nullable = false)
    @Builder.Default
    private Integer bookmarkCount = 0;

    @Column(name = "RENAME_FILENAME")
    private String renameFilename;

    @Column(name = "ORIGINAL_FILENAME")
    private String originalFilename;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATE_AT")
    private Date updateAt;

    // 좋아요 카운트 증가
    public void incrementLikeCount() {
        this.likeCount = this.likeCount + 1;
    }

    // 좋아요 카운트 감소
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount = this.likeCount - 1;
        }
    }

    // 북마크 카운트 증가
    public void incrementBookmarkCount() {
        this.bookmarkCount = this.bookmarkCount + 1;
    }

    // 북마크 카운트 감소
    public void decrementBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount = this.bookmarkCount - 1;
        }
    }

    public Board toDto() {
        return Board.builder()
                .contentId(contentId)
                .userId(userId)
                .title(title)
                .contentBody(contentBody)
                .contentType(contentType)
                .category(category)
                .tags(tags)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .bookmarkCount(bookmarkCount)
                .renameFilename(renameFilename)
                .originalFilename(originalFilename)
                .createdAt(createdAt)
                .updateAt(updateAt)
                .build();
    }
}
