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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    @Autowired
    private final NoticeRepository noticeRepository;


    @Transactional(readOnly = true)
    public Page<Notice> selectList(Pageable pageable) {
        Page<NoticeEntity> noticePage = noticeRepository.findAllByContentType("notice", pageable);
        return noticePage.map(NoticeEntity::toDto);
    }

    public Notice selectNotice(int contentId) {
        NoticeEntity noticeEntity = noticeRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다. id=" + contentId));

        return noticeEntity.toDto();
    }


    public void updateAddViewCount(int contentId) {

        noticeRepository.findById(contentId).ifPresent(entity -> {
            entity.setViewCount(entity.getViewCount() + 1);
            noticeRepository.save(entity);
        });
    }

    public int insertNotice(Notice notice) {
        try {
            NoticeEntity noticeEntity = notice.toEntity();

            noticeEntity.setTags("고양이,강아지,반려동물");

            noticeRepository.save(noticeEntity);

            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public Page<Notice> selectSearchTitle(String keyword, Pageable pageable) {
        Page<NoticeEntity> entityPage = noticeRepository.findByTitleContainingAndContentType(keyword, "notice", pageable);

        return entityPage.map(NoticeEntity::toDto);
    }

    public void deleteNotice(int noticeId) {
        if (noticeRepository.existsById(noticeId)) {
            noticeRepository.deleteById(noticeId);
        } else {
            throw new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다. id=" + noticeId);
        }
    }


    public Notice updateNotice(Notice notice, MultipartFile file) {
        NoticeEntity existingEntity = noticeRepository.findById(notice.getContentId())
                .orElse(null);

        if (existingEntity == null) {
            return null;
        }

        if (existingEntity.getTags() == null) {
            existingEntity.setTags("고양이,강아지,반려동물");
        }
        existingEntity.setTitle(notice.getTitle());
        existingEntity.setContentBody(notice.getContentBody());
        existingEntity.setOriginalFilename(notice.getOriginalFilename());
        existingEntity.setRenameFilename(notice.getRenameFilename());

        NoticeEntity updatedEntity = noticeRepository.save(existingEntity);

        return updatedEntity.toDto();
    }



    @Transactional(readOnly = true)
    public List<Notice> findLatestNotices() { // ✅ 반환 타입을 List<Notice>로 변경
        List<NoticeEntity> entities = noticeRepository.findTop5ByContentTypeOrderByCreatedAtDesc("notice");

        // ✅ toSummaryDto() 대신 기존 toDto()를 사용
        return entities.stream()
                .map(NoticeEntity::toDto)
                .collect(Collectors.toList());
    }
}
