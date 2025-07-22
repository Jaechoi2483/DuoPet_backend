package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    // 특정 유저가 특정 게시글에 좋아요 눌렀는지 여부 (중복 방지용)
    Optional<LikeEntity> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
    // 좋아요 취소 시 사용 (userId + contentId + 유형으로 삭제)
    void deleteByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
    // 마이페이지용: 특정 사용자의 좋아요 전체 목록 조회
    List<LikeEntity> findAllByUserId(Long userId);

    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
}
