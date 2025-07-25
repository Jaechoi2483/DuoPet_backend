package com.petlogue.duopetbackend.notice.jpa.repository;

import com.petlogue.duopetbackend.notice.jpa.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {
    Page<NoticeEntity> findAllByContentType(String contentType, Pageable pageable);

    Page<NoticeEntity> findByTitleContainingAndContentType(String keyword, String contentType, Pageable pageable);
    List<NoticeEntity> findTop5ByContentTypeOrderByCreatedAtDesc(String contentType);

}
