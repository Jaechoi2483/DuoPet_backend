package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class QnaConsultationDto {
    
    // Q&A 상담 생성 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long vetId;         // 상담 요청할 수의사 ID
        private Long petId;         // 상담 대상 반려동물 ID
        private String title;       // 질문 제목
        private String content;     // 질문 내용
        private String category;    // 카테고리 (건강, 행동, 영양, 기타)
        private List<MultipartFile> files; // 첨부파일
    }
    
    // Q&A 답변 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerRequest {
        private String content;     // 답변 내용
        private List<MultipartFile> files; // 첨부파일
    }
    
    // Q&A 상담 응답 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long roomId;
        private String roomUuid;
        private Long userId;
        private String userName;
        private Long vetId;
        private String vetName;
        private String vetSpecialty;
        private Long petId;
        private String petName;
        private String petSpecies;  // 품종
        private String petGender;   // 성별
        private Integer petAge;     // 나이
        private String roomStatus;
        private String title;
        private String category;
        private BigDecimal consultationFee;
        private String paymentStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
        private List<MessageDto> messages;
        private boolean hasAnswer;
        private int totalMessages;
    }
    
    // 메시지 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDto {
        private Long messageId;
        private String senderType; // USER, VET
        private String senderName;
        private String messageType; // TEXT, IMAGE, FILE
        private String content;
        private String fileUrl;
        private String fileName;
        private Long fileSize;
        private boolean isRead;
        private boolean isMainQuestion; // 주 질문 여부
        private boolean isMainAnswer;   // 주 답변 여부
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
}