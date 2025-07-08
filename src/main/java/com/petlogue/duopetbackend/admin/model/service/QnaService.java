package com.petlogue.duopetbackend.admin.model.service;

import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
import com.petlogue.duopetbackend.admin.jpa.repository.QnaRepository;
import com.petlogue.duopetbackend.admin.model.dto.Qna;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QnaService {
    private final QnaRepository qnaRepository;

    public Page<Qna> findAllQna(Pageable pageable) {
        // 1. Repository를 통해 데이터베이스에서 Page<QnaEntity>를 조회합니다.
        //    이때 contentType이 'qna'인 것만 필터링해야 합니다.
        Page<QnaEntity> qnaPage = qnaRepository.findByContentType("qna", pageable);

        // 2. 조회된 Page<QnaEntity>의 각 엔티티를 DTO로 변환합니다.
        //    Page 객체의 map 기능을 사용하면 페이징 정보는 그대로 유지됩니다.
        return qnaPage.map(QnaEntity::toDto);
    }

}
