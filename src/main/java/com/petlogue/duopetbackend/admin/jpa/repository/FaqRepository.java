package com.petlogue.duopetbackend.admin.jpa.repository;

import com.petlogue.duopetbackend.admin.jpa.entity.FaqEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<FaqEntity, Long> {
}
