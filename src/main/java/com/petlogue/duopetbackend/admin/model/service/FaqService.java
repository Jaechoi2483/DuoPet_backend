package com.petlogue.duopetbackend.admin.model.service;

import com.petlogue.duopetbackend.admin.jpa.entity.FaqEntity;
import com.petlogue.duopetbackend.admin.jpa.repository.FaqRepository;
import com.petlogue.duopetbackend.admin.model.dto.Faq;
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
public class FaqService {

    private final FaqRepository faqRepository;

    @Transactional(readOnly = true)
    public List<Faq> findAllFaqs() {
        // 1. 데이터베이스에서 모든 FaqEntity를 조회합니다.
        List<FaqEntity> entities = faqRepository.findAll();

        // 2. 조회된 엔티티 리스트를 DTO 리스트로 변환합니다.
        //    stream().map()을 사용하여 각 엔티티의 toDto() 메소드를 호출합니다.
        return entities.stream()
                .map(FaqEntity::toDto)
                .collect(Collectors.toList());
    }

    public Faq updateFaq(int faqId, Faq updateRequest) {
        // 1. ID로 기존 FAQ 엔티티를 찾음
        FaqEntity faqEntity = faqRepository.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("해당 FAQ를 찾을 수 없습니다. id=" + faqId));

        // 2. 찾아온 엔티티의 내용을 받은 DTO의 내용으로 변경 (dirty-checking)
        faqEntity.setQuestion(updateRequest.getQuestion());
        faqEntity.setAnswer(updateRequest.getAnswer());

        // 3. @Transactional에 의해 메소드가 끝나면 변경된 내용이 자동으로 DB에 저장됨
        //    명시적으로 save를 호출해도 무방합니다: faqRepository.save(faqEntity);

        // 4. 수정된 엔티티를 다시 DTO로 변환하여 반환
        return faqEntity.toDto();
    }

    public Page<Faq> findFaqs(String keyword, Pageable pageable) {
        Page<FaqEntity> faqPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드가 있으면 질문과 답변 내용에서 모두 검색
            faqPage = faqRepository.findByQuestionContainingOrAnswerContaining(keyword, keyword, pageable);
        } else {
            // 키워드가 없으면 전체 조회
            faqPage = faqRepository.findAll(pageable);
        }
        return faqPage.map(FaqEntity::toDto);
    }

    public FaqEntity createFaq(Faq requestDto, Integer userId) {
        FaqEntity faqEntity = FaqEntity.builder()
                .question(requestDto.getQuestion())
                .answer(requestDto.getAnswer())
                .userId(userId)
                .build();
        return faqRepository.save(faqEntity);
    }

    public boolean deleteFaq(int faqId) {
        // 해당 ID의 FAQ가 존재하는지 먼저 확인
        if (faqRepository.existsById(faqId)) {
            faqRepository.deleteById(faqId);
            return true; // 삭제 성공
        }
        return false; // 삭제할 대상이 없음
    }
}
