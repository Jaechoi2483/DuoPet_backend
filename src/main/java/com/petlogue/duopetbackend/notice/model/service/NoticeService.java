package com.petlogue.duopetbackend.notice.model.service;


import com.petlogue.duopetbackend.notice.jpa.entity.NoticeEntity;
import com.petlogue.duopetbackend.notice.jpa.repository.NoticeRepository;
import com.petlogue.duopetbackend.notice.model.dto.Notice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    @Autowired
    private final NoticeRepository noticeRepository;


    @Transactional(readOnly = true)
    public Page<Notice> selectList(Pageable pageable) {
        // 1. Repository에서 Page<NoticeEntity>를 조회
        Page<NoticeEntity> noticePage = noticeRepository.findAll(pageable);

        // 2. Page<NoticeEntity>를 Page<NoticeDto>로 변환
        return noticePage.map(NoticeEntity::toDto);
    }
}
