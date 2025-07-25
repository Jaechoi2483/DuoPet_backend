package com.petlogue.duopetbackend.admin.jpa.entity;

import com.petlogue.duopetbackend.admin.model.dto.Qna;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "CONTENT")
@Entity
public class QnaEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONTENT_ID")
    private int contentId;

    @Column(name = "USER_ID", nullable = false)
    private int userId;

    @Column(name = "TITLE", nullable = false, length = 1000)
    private String title;

    @Lob
    @Column(name = "CONTENT_BODY", nullable = false)
    private String contentBody;

    @Column(name = "CONTENT_TYPE", nullable = false, length = 50)
    private String  contentType;

    @Column(name = "CATEGORY", length = 200)
    private String  category;

    @Column(name = "TAGS", length = 100)
    private String tags;

    @Column(name = "VIEW_COUNT", nullable = false)
    private int viewCount;
    @Column(name = "LIKE_COUNT")
    private int likeCount;

    @Column(name = "RENAME_FILENAME", length = 255)
    private String renameFilename;

    @Column(name = "ORIGINAL_FILENAME", length = 255)
    private String originalFilename;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATE_AT")
    private LocalDateTime updatedAt;


   

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaAnswerEntity> answers = new ArrayList<>();




    public Qna toDto(){
        String status = (this.answers != null && !this.answers.isEmpty())
                ? "ANSWERED"
                : "PENDING";
        return Qna.builder()
                .contentId(contentId)
                .userId(userId)
                .title(title)
                .contentBody(contentBody)
                .contentType(contentType)
                .category(category)
                .tags(tags)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .renameFilename(renameFilename)
                .originalFilename(originalFilename)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .status(this.getAnswers() == null || this.getAnswers().isEmpty() ? "PENDING" : "ANSWERED")
                .build();
    }


}
