package com.petlogue.duopetbackend.board.model.dto;

import com.petlogue.duopetbackend.board.jpa.entity.BoardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board {
    private Long  contentId;          // 게시글 PK
    private Long  userId;             // 작성자 ID
    private String title;             // 제목
    private String contentBody;       // 본문 내용
    private String contentType;       // 게시글 유형 (board, notice 등)
    private String category;          // 카테고리 (free, review 등)
    private String tags;              // 태그 (콤마 구분)
    private Integer  viewCount;        // 조회수
    private Integer  likeCount;        // 좋아요 수
    private Integer  bookmarkCount;    // 북마크 수
    private String renameFilename;    // 서버 저장 파일명
    private String originalFilename;  // 원본 파일명
    private Date createdAt;      // 작성일
    private Date updateAt;       // 수정일
    @Builder.Default
    private String status = "ACTIVE";

    public BoardEntity toEntity() {
        return BoardEntity.builder()
                .contentId(contentId)           // update 시 필요, insert 시 생략 가능
                .userId(userId)
                .title(title)
                .contentBody(contentBody)
                .contentType(contentType)
                .category(category)
                .tags(tags)
                .viewCount(viewCount != null ? viewCount : 0)
                .likeCount(likeCount != null ? likeCount : 0)
                .bookmarkCount(bookmarkCount != null ? bookmarkCount : 0)
                .renameFilename(renameFilename)
                .originalFilename(originalFilename)
                .createdAt(createdAt)
                .updateAt(updateAt)
                .status(this.status)
                .build();
    }
}
