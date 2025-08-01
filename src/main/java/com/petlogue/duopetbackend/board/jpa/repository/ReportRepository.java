package com.petlogue.duopetbackend.board.jpa.repository;

import com.petlogue.duopetbackend.board.jpa.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    boolean existsByUser_UserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReportEntity r WHERE r.targetId = :targetId AND r.targetType = :targetType")
    void deleteAllByTargetIdAndTargetType(Long targetId, String targetType);

    // 관리자 페이지 신고관리용
    List<ReportEntity> findAllByOrderByCreatedAtDesc();

    Page<ReportEntity> findAllByStatusOrderByCreatedAtDesc(String status, Pageable pageable);


    Page<ReportEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
