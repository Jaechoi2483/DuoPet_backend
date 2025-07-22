package com.petlogue.duopetbackend.board.jpa.entity;

import com.petlogue.duopetbackend.board.model.dto.Like;
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
@Table(name= "\"LIKE\"")
public class LikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LIKE_ID")
    private Long likeId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "TARGET_ID", nullable = false)
    private Long targetId;

    @Column(name = "TARGET_TYPE", nullable = false, length = 50)
    private String targetType; // "board", "comment" 등

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    // 생성자 수정 (targetId, userId, targetType을 받도록)
    public LikeEntity(Long userId, Long targetId, String targetType) {
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public Like toLikeDto(){
        return Like.builder()
                .likeId(this.likeId)
                .userId(this.userId)
                .targetId(this.targetId)
                .targetType(this.targetType)
                .createdAt(this.createdAt)
                .build();
    }
}
