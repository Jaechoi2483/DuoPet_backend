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
        List<FaqEntity> entities = faqRepository.findAll();

        return entities.stream()
                .map(FaqEntity::toDto)
                .collect(Collectors.toList());
    }

    public Faq updateFaq(int faqId, Faq updateRequest) {
        FaqEntity faqEntity = faqRepository.findById(faqId)
                .orElseThrow(() -> new IllegalArgumentException("해당 FAQ를 찾을 수 없습니다. id=" + faqId));

        faqEntity.setQuestion(updateRequest.getQuestion());
        faqEntity.setAnswer(updateRequest.getAnswer());

        return faqEntity.toDto();
    }

    public Page<Faq> findFaqs(String keyword, Pageable pageable) {
        Page<FaqEntity> faqPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            faqPage = faqRepository.findByQuestionContainingOrAnswerContaining(keyword, keyword, pageable);
        } else {
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
        if (faqRepository.existsById(faqId)) {
            faqRepository.deleteById(faqId);
            return true;
        }
        return false;
    }
}
