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
    public Notice updateNotice(Notice notice, MultipartFile file) {
        // 1. 기존 엔티티를 ID로 조회합니다.
        NoticeEntity existingEntity = noticeRepository.findById(notice.getContentId())
                .orElse(null); // orElse(null)은 예시이며, 실제로는 예외 처리가 더 좋습니다.

        if (existingEntity == null) {
            // 수정할 게시물이 없으면 null 반환
            return null;
        }

        // 2. DTO의 내용으로 기존 엔티티의 값을 변경합니다.
        existingEntity.setTitle(notice.getTitle());
        existingEntity.setContentBody(notice.getContentBody());
        existingEntity.setOriginalFilename(notice.getOriginalFilename());
        existingEntity.setRenameFilename(notice.getRenameFilename());

        // 3. JpaRepository는 변경된 내용을 감지하여 자동으로 UPDATE 쿼리를 실행합니다.
        // 별도의 save() 호출이 필요 없을 수 있으나 명시적으로 호출하는 것도 안전합니다.
        NoticeEntity updatedEntity = noticeRepository.save(existingEntity);

        return updatedEntity.toDto();
    }
}
