package com.petlogue.duopetbackend.notice.jpa.repository;

import com.petlogue.duopetbackend.notice.jpa.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
}
