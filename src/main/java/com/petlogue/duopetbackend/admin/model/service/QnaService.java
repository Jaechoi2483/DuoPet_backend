package com.petlogue.duopetbackend.admin.model.service;

import com.petlogue.duopetbackend.admin.jpa.entity.QnaAnswerEntity;
import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
import com.petlogue.duopetbackend.admin.jpa.repository.QnaRepository;
import com.petlogue.duopetbackend.admin.model.dto.Qna;
import com.petlogue.duopetbackend.admin.model.dto.QnaAnswer;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Page<QnaEntity> qnaEntityPage;

        if ("ANSWERED".equals(status)) {
            qnaEntityPage = qnaRepository.findAnsweredQnaForAdmin(pageable);
        } else if ("PENDING".equals(status)) {
            qnaEntityPage = qnaRepository.findPendingQnaForAdmin(pageable);
        } else { // status가 "ALL"이거나 다른 값일 경우 전체 조회
            qnaEntityPage = qnaRepository.findByContentType("qna", pageable);
        }

        return qnaEntityPage.map(QnaEntity::toDto);
    }

    /**
     * 일반 사용자용 Q&A 목록 조회 (상태별 필터링)
     */
    @Transactional(readOnly = true)
    public Page<Qna> findAllQnaByUser(String loginId, Pageable pageable, String status) {
        // loginId로 사용자의 숫자 ID(PK)를 조회합니다.
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("User not found: " + loginId));
        Integer userId = user.getUserId().intValue();

        Page<QnaEntity> qnaEntityPage;

        if ("ANSWERED".equals(status)) {
            qnaEntityPage = qnaRepository.findAnsweredQnaByUser(userId, pageable);
        } else if ("PENDING".equals(status)) {
            qnaEntityPage = qnaRepository.findPendingQnaByUser(userId, pageable);
        } else { // status가 "ALL"이거나 다른 값일 경우 해당 유저의 전체 QNA 조회
            qnaEntityPage = qnaRepository.findByUserIdAndContentType(userId, "qna", pageable);
        }

        return qnaEntityPage.map(QnaEntity::toDto);
    }

    @Transactional(readOnly = true)
    public Qna findQnaDetail(int contentId) {
        // 복원된 레포지토리 메소드 사용
        QnaEntity qnaEntity = qnaRepository.findByIdWithAnswers(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. id=" + contentId));

        Qna qnaDto = qnaEntity.toDto();

        // List<QnaAnswerEntity>를 List<QnaAnswer>로 변환
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
            qnaRepository.save(qnaEntity);

            return 1; // 성공
        } catch (Exception e) {
            return 0; // 실패
        }
    }

    @Transactional
    public int saveAnswer(int qnaId, String content, Integer adminUserId) {
        try {
            // 1. 질문 엔티티 조회
            QnaEntity qnaEntity = qnaRepository.findById(qnaId) // int qnaId를 그대로 사용
                    .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. id=" + qnaId));
            // 2. 새 답변 엔티티 생성 및 필드 설정
            QnaAnswerEntity answerEntity = new QnaAnswerEntity();
            answerEntity.setContent(content);
            answerEntity.setUserId(adminUserId); // 관리자 ID 저장 (DB용, UI에는 노출 안 함)
            answerEntity.setQna(qnaEntity);

            // 3. 질문 엔티티의 답변 리스트에 추가
            List<QnaAnswerEntity> answers = qnaEntity.getAnswers();
            if (answers == null) {
                answers = new ArrayList<>();
                qnaEntity.setAnswers(answers);
            }
            answers.add(answerEntity);

            // 4. QnaEntity 저장 (답변도 함께 저장됨)
            qnaRepository.save(qnaEntity);

            return 1; // 성공
        } catch (Exception e) {
            // 로깅 추가 가능: log.error("답변 저장 실패", e);
            return 0; // 실패
        }
    }

}
