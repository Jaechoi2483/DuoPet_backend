package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM LikeEntity l WHERE l.targetId = :targetId AND l.targetType = :targetType")
    void deleteAllByTargetIdAndTargetType(Long targetId, String targetType);

    // 특정 유저가 특정 게시글에 좋아요 눌렀는지 여부 (중복 방지용)
    Optional<LikeEntity> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
    // 좋아요 취소 시 사용 (userId + contentId + 유형으로 삭제)
    void deleteByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
    // 마이페이지용: 특정 사용자의 좋아요 전체 목록 조회
    List<LikeEntity> findAllByUserId(Long userId);

    /**
     * 특정 사용자와 대상 ID, 타입으로 좋아요가 존재하는지 확인
     */
    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
}
