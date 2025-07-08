package com.petlogue.duopetbackend.admin.model.service;

import com.petlogue.duopetbackend.admin.jpa.entity.FaqEntity;
import com.petlogue.duopetbackend.admin.jpa.repository.FaqRepository;
import com.petlogue.duopetbackend.admin.model.dto.Faq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
