package com.petlogue.duopetbackend.admin.jpa.repository;


import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QnaRepository extends JpaRepository<QnaEntity, Integer> {




    Page<QnaEntity> findByContentType(String contentType, Pageable pageable);
    Page<QnaEntity> findByUserIdAndContentType(Integer userId, String contentType, Pageable pageable);



    @Query("SELECT q FROM QnaEntity q LEFT JOIN FETCH q.answers WHERE q.contentId = :contentId")
    Optional<QnaEntity> findByIdWithAnswers(@Param("contentId") int contentId);

    @Query("SELECT q FROM QnaEntity q WHERE q.contentType = 'qna' AND q.answers IS NOT EMPTY")
    Page<QnaEntity> findAnsweredQnaForAdmin(Pageable pageable);

    // 답변 대기중인 모든 글 조회 (answers 리스트가 비어있는 경우)
    @Query("SELECT q FROM QnaEntity q WHERE q.contentType = 'qna' AND q.answers IS EMPTY")
    Page<QnaEntity> findPendingQnaForAdmin(Pageable pageable);

    // --- 사용자용 필터링 쿼리 ---

    // 특정 사용자가 작성한 글 중 답변 완료된 글 조회
    @Query("SELECT q FROM QnaEntity q WHERE q.userId = :userId AND q.contentType = 'qna' AND q.answers IS NOT EMPTY")
    Page<QnaEntity> findAnsweredQnaByUser(@Param("userId") Integer userId, Pageable pageable);

    // 특정 사용자가 작성한 글 중 답변 대기중인 글 조회
    @Query("SELECT q FROM QnaEntity q WHERE q.userId = :userId AND q.contentType = 'qna' AND q.answers IS EMPTY")
    Page<QnaEntity> findPendingQnaByUser(@Param("userId") Integer userId, Pageable pageable);


}
