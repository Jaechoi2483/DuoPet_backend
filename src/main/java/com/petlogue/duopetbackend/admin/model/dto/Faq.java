package com.petlogue.duopetbackend.admin.model.dto;


import com.petlogue.duopetbackend.admin.jpa.entity.FaqEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Faq {
    private int faqId;
    private int userId;
    private String question;
    private String answer;

    public FaqEntity toEntity() {
        return FaqEntity.builder()
                .faqId(faqId)
                .userId(userId)
                .question(question)
                .answer(answer)
                .build();
    }
}
