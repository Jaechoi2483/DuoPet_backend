package com.petlogue.duopetbackend.admin.model.service;

import com.petlogue.duopetbackend.admin.jpa.entity.QnaAnswerEntity;
import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
import com.petlogue.duopetbackend.admin.jpa.repository.QnaRepository;
import com.petlogue.duopetbackend.admin.model.dto.Qna;
import com.petlogue.duopetbackend.admin.model.dto.QnaAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public Qna findQnaDetail(int contentId) {
        // 1. QnaEntity 조회
        QnaEntity qnaEntity = qnaRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. id=" + contentId));

        // 2. QnaEntity를 기본 Qna DTO로 변환
        Qna qnaDto = qnaEntity.toDto();

        // 3. 답변 엔티티 목록을 답변 DTO 목록으로 변환
        List<QnaAnswer> answerDtos = qnaEntity.getAnswers().stream()
                .map(QnaAnswerEntity::toDto)
                .collect(Collectors.toList());

        // 4. 변환된 답변 DTO 목록을 Qna DTO에 설정
        qnaDto.setAnswers(answerDtos);

        return qnaDto;
    }
}
