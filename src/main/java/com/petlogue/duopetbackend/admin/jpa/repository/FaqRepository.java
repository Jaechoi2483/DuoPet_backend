package com.petlogue.duopetbackend.admin.jpa.repository;

import com.petlogue.duopetbackend.admin.jpa.entity.FaqEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<FaqEntity, Integer> {

    Page<FaqEntity> findByQuestionContainingOrAnswerContaining(String question, String answer, Pageable pageable);
}
