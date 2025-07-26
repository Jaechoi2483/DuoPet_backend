package com.petlogue.duopetbackend.admin.jpa.repository;


import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QnaRepository extends JpaRepository<QnaEntity, Integer>, JpaSpecificationExecutor<QnaEntity> {




    Page<QnaEntity> findByContentType(String contentType, Pageable pageable);
    Page<QnaEntity> findByUserIdAndContentType(Integer userId, String contentType, Pageable pageable);



    @Query("SELECT q FROM QnaEntity q LEFT JOIN FETCH q.answers WHERE q.contentId = :contentId")
    Optional<QnaEntity> findByIdWithAnswers(@Param("contentId") int contentId);


}
