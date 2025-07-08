package com.petlogue.duopetbackend.admin.jpa.entity;


import com.petlogue.duopetbackend.admin.model.dto.Faq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name= "FAQ")
@Entity
public class FaqEntity {

    @Id
    @Column(name = "FAQ_ID")
    private int faqId;
    @Column(name = "USER_ID", nullable = false)
    private int userId;
    @Column(name = "QUESTION", nullable = false, length = 1000)
    private String question;
    @Column(name = "ANSWER",nullable = false,  length = 4000)
    private String answer;

    public Faq toDto(){
        return Faq.builder()
                .faqId(this.faqId)
                .question(this.question)
                .answer(this.answer)
                .userId(this.userId)
                .build();
    }
}
