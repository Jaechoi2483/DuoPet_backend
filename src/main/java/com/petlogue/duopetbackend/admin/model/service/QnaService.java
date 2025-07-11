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
            // DTO를 Entity로 변환
            QnaEntity qnaEntity = qna.toEntity();

            // Entity에 전달받은 userId를 명시적으로 설정
            qnaEntity.setUserId(userId);

            qnaEntity.setContentType("qna");
            // 작성자 ID가 포함된 완전한 엔티티를 저장
            qnaRepository.save(qnaEntity);

            return 1; // 성공
        } catch (Exception e) {
            // log.error("QnA 저장 중 오류 발생: {}", e.getMessage(), e);
            return 0; // 실패
        }
    }

}
