package com.petlogue.duopetbackend.admin.model.service;

import com.petlogue.duopetbackend.admin.jpa.entity.QnaAnswerEntity;
import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
import com.petlogue.duopetbackend.admin.jpa.repository.QnaRepository;
import com.petlogue.duopetbackend.admin.model.dto.Qna;
import com.petlogue.duopetbackend.admin.model.dto.QnaAnswer;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QnaService {
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<Qna> findAllQnaForAdmin(Pageable pageable, String status) {
        Specification<QnaEntity> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("contentType"), "qna");

            if ("ANSWERED".equalsIgnoreCase(status)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNotEmpty(root.get("answers")));
            } else if ("PENDING".equalsIgnoreCase(status)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.isEmpty(root.get("answers")));
            }
            return predicate;
        };

        Page<QnaEntity> qnaEntityPage = qnaRepository.findAll(spec, pageable);
        log.info("서비스 결과: 총 페이지 = {}, 총 개수 = {}, 현재 페이지 = {}",
                qnaEntityPage.getTotalPages(),
                qnaEntityPage.getTotalElements(),
                qnaEntityPage.getNumber());
        return qnaEntityPage.map(QnaEntity::toDto);
    }

    @Transactional(readOnly = true)
    public Page<Qna> findAllQnaByUser(String loginId, Pageable pageable, String status) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("User not found: " + loginId));
        Integer userId = user.getUserId().intValue();

        Specification<QnaEntity> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("contentType"), "qna"),
                    criteriaBuilder.equal(root.get("userId"), userId)
            );

            if ("ANSWERED".equalsIgnoreCase(status)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNotEmpty(root.get("answers")));
            } else if ("PENDING".equalsIgnoreCase(status)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.isEmpty(root.get("answers")));
            }
            return predicate;
        };

        // 2. Repository의 findAll 메소드에 Specification과 Pageable을 전달
        Page<QnaEntity> qnaEntityPage = qnaRepository.findAll(spec, pageable);
        return qnaEntityPage.map(QnaEntity::toDto);
    }

    @Transactional(readOnly = true)
    public Qna findQnaDetail(int contentId) {
        QnaEntity qnaEntity = qnaRepository.findByIdWithAnswers(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. id=" + contentId));

        Qna qnaDto = qnaEntity.toDto();

        List<QnaAnswer> answerDtos = qnaEntity.getAnswers().stream()
                .map(QnaAnswerEntity::toDto)
                .collect(Collectors.toList());

        qnaDto.setAnswers(answerDtos);

        return qnaDto;
    }


    @Transactional
    public int saveQna(Qna qna, Integer userId) {
        try {
            QnaEntity qnaEntity = qna.toEntity();
            qnaEntity.setUserId(userId);
            qnaEntity.setContentType("qna");
            qnaEntity.setTags("고양이,강아지,반려동물");
            qnaRepository.save(qnaEntity);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public int saveAnswer(int qnaId, String content, Integer adminUserId) {
        try {
            QnaEntity qnaEntity = qnaRepository.findById(qnaId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. id=" + qnaId));
            QnaAnswerEntity answerEntity = new QnaAnswerEntity();
            answerEntity.setContent(content);
            answerEntity.setUserId(adminUserId);
            answerEntity.setQna(qnaEntity);

            List<QnaAnswerEntity> answers = qnaEntity.getAnswers();
            if (answers == null) {
                answers = new ArrayList<>();
                qnaEntity.setAnswers(answers);
            }
            answers.add(answerEntity);

            qnaRepository.save(qnaEntity);

            return 1; // 성공
        } catch (Exception e) {
            return 0; // 실패
        }
    }

}
