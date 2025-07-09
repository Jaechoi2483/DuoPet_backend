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
        // "notice" 타입의 콘텐츠만 조회하도록 변경
        Page<NoticeEntity> noticePage = noticeRepository.findAllByContentType("notice", pageable);
        return noticePage.map(NoticeEntity::toDto);
    }
    public Notice selectNotice(int contentId) {
        // ID로 엔티티를 찾고, 만약 없다면 예외(Exception)를 발생시킵니다.
        NoticeEntity noticeEntity = noticeRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다. id=" + contentId));

        // 예외가 발생하지 않았다면(엔티티를 찾았다면) DTO로 변환하여 반환합니다.
        return noticeEntity.toDto();
    }


    public void updateAddViewCount(int contentId) {

        noticeRepository.findById(contentId).ifPresent(entity -> {
            entity.setViewCount(entity.getViewCount() + 1);
            noticeRepository.save(entity);
        });
    }

    public int insertNotice(Notice notice) {

        NoticeEntity savedEntity = noticeRepository.save(notice.toEntity());
        return savedEntity != null ? 1 : 0;
    }

    public Page<Notice> selectSearchTitle(String keyword, Pageable pageable) {
        Page<NoticeEntity> entityPage = noticeRepository.findByTitleContainingAndContentType(keyword, "notice", pageable);

        return entityPage.map(NoticeEntity::toDto);
    }

    public void deleteNotice(int noticeId) {
        // 해당 ID의 공지사항이 존재하는지 확인 후 삭제
        if (noticeRepository.existsById(noticeId)) {
            noticeRepository.deleteById(noticeId);
        } else {
            throw new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다. id=" + noticeId);
        }
    }

}
