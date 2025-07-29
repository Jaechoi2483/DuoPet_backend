package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.consultation.jpa.entity.ChatMessage;
import com.petlogue.duopetbackend.consultation.jpa.entity.ConsultationRoom;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetProfile;
import com.petlogue.duopetbackend.consultation.jpa.repository.ChatMessageRepository;
import com.petlogue.duopetbackend.consultation.jpa.repository.ConsultationRoomRepository;
import com.petlogue.duopetbackend.consultation.model.dto.QnaConsultationDto;
import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.jpa.repository.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QnaConsultationService {
    
    private final ConsultationRoomRepository consultationRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final VetRepository vetRepository;
    private final PetRepository petRepository;
    private final VetProfileService vetProfileService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // Q&A 상담 생성
    public QnaConsultationDto.Response createQnaConsultation(
            Long userId, 
            QnaConsultationDto.CreateRequest request) {
        
        // 사용자, 수의사, 반려동물 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        VetEntity vet = vetRepository.findById(request.getVetId())
                .orElseThrow(() -> new RuntimeException("수의사를 찾을 수 없습니다."));
        
        PetEntity pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("반려동물을 찾을 수 없습니다."));
        
        // 반려동물 소유자 확인
        if (!pet.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인의 반려동물만 상담 가능합니다.");
        }
        
        // 수의사 프로필에서 Q&A 상담료 조회
        BigDecimal consultationFee;
        try {
            VetProfile vetProfile = vetProfileService.getVetProfile(request.getVetId());
            consultationFee = vetProfile.getConsultationFee();
        } catch (Exception e) {
            // 프로필이 없는 경우 기본 상담료 사용
            consultationFee = new BigDecimal("30000");
        }
        
        // ConsultationRoom 생성
        ConsultationRoom room = ConsultationRoom.builder()
                .user(user)
                .vet(vet)
                .pet(pet)
                .consultationType(ConsultationRoom.ConsultationType.QNA.name())
                .roomStatus(ConsultationRoom.RoomStatus.CREATED.name())
                .consultationFee(consultationFee)
                .paymentStatus(ConsultationRoom.PaymentStatus.PENDING.name())
                .chiefComplaint(request.getTitle()) // 제목을 주요 증상으로 저장
                .consultationNotes(request.getCategory()) // 카테고리를 상담 노트에 임시 저장
                .build();
        
        room = consultationRoomRepository.save(room);
        
        // 첫 번째 메시지로 질문 내용 저장
        ChatMessage questionMessage = ChatMessage.builder()
                .consultationRoom(room)
                .sender(user)
                .senderType(ChatMessage.SenderType.USER.name())
                .messageType(ChatMessage.MessageType.TEXT.name())
                .content(request.getContent())
                .isImportant("Y") // 주 질문은 중요 표시
                .build();
        
        chatMessageRepository.save(questionMessage);
        
        // 파일 업로드 처리
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            processFileUploads(room, user, request.getFiles());
        }
        
        // 응답 DTO 생성
        return convertToResponse(room);
    }
    
    // Q&A 답변 작성
    public QnaConsultationDto.Response createAnswer(
            Long userId, 
            Long roomId, 
            QnaConsultationDto.AnswerRequest request) {
        
        // 상담방 조회
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("상담을 찾을 수 없습니다."));
        
        // 수의사 확인
        if (!room.getVet().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인에게 요청된 상담만 답변 가능합니다.");
        }
        
        // 상담 타입 확인
        if (!ConsultationRoom.ConsultationType.QNA.name().equals(room.getConsultationType())) {
            throw new RuntimeException("Q&A 상담만 답변 가능합니다.");
        }
        
        // 답변 메시지 저장
        // userId로 VetEntity를 찾아야 함
        VetEntity vet = vetRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("수의사를 찾을 수 없습니다."));
        
        UserEntity vetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("수의사 사용자를 찾을 수 없습니다."));
        
        ChatMessage answerMessage = ChatMessage.builder()
                .consultationRoom(room)
                .sender(vetUser)
                .senderType(ChatMessage.SenderType.VET.name())
                .messageType(ChatMessage.MessageType.TEXT.name())
                .content(request.getContent())
                .isImportant("Y") // 주 답변은 중요 표시
                .build();
        
        chatMessageRepository.save(answerMessage);
        
        // 파일 업로드 처리
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            processFileUploads(room, vetUser, request.getFiles());
        }
        
        // 상담 상태를 답변완료로 변경
        room.setRoomStatus(ConsultationRoom.RoomStatus.COMPLETED.name());
        // consultationNotes는 카테고리 정보를 저장하고 있으므로 변경하지 않음
        room.setEndedAt(LocalDateTime.now());
        
        return convertToResponse(room);
    }
    
    // 사용자의 Q&A 상담 목록 조회
    @Transactional(readOnly = true)
    public Page<QnaConsultationDto.Response> getUserQnaConsultations(
            Long userId, Pageable pageable) {
        
        Page<ConsultationRoom> rooms = consultationRoomRepository
                .findByUserIdAndConsultationType(userId, 
                        ConsultationRoom.ConsultationType.QNA.name(), pageable);
        
        return rooms.map(this::convertToResponse);
    }
    
    // 수의사의 Q&A 상담 목록 조회
    @Transactional(readOnly = true)
    public Page<QnaConsultationDto.Response> getVetQnaConsultations(
            Long vetId, String status, Pageable pageable) {
        
        Page<ConsultationRoom> rooms;
        if (status != null && !status.isEmpty()) {
            rooms = consultationRoomRepository
                    .findByVetIdAndConsultationTypeAndRoomStatus(
                            vetId, 
                            ConsultationRoom.ConsultationType.QNA.name(), 
                            status, 
                            pageable);
        } else {
            rooms = consultationRoomRepository
                    .findByVetIdAndConsultationType(
                            vetId, 
                            ConsultationRoom.ConsultationType.QNA.name(), 
                            pageable);
        }
        
        return rooms.map(this::convertToResponse);
    }
    
    // Q&A 상담 상세 조회
    @Transactional(readOnly = true)
    public QnaConsultationDto.Response getQnaConsultationDetail(
            Long roomId, Long userId) {
        
        ConsultationRoom room = consultationRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("상담을 찾을 수 없습니다."));
        
        // 접근 권한 확인 (상담 요청자 또는 담당 수의사만 조회 가능)
        if (!room.getUser().getUserId().equals(userId) && 
            !room.getVet().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("상담 내역을 조회할 권한이 없습니다.");
        }
        
        return convertToResponse(room);
    }
    
    // 파일 업로드 처리
    private void processFileUploads(ConsultationRoom room, UserEntity sender, 
                                    List<MultipartFile> files) {
        String subPath = "consultation/" + room.getRoomId();
        String fullPath = uploadDir + "/" + subPath;
        
        // 디렉토리 생성
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    // 파일명 변경
                    String originalFileName = file.getOriginalFilename();
                    String storedFileName = FileNameChange.change(originalFileName, "yyyyMMddHHmmss");
                    String filePath = fullPath + "/" + storedFileName;
                    
                    // 파일 저장
                    file.transferTo(new File(filePath));
                    
                    // 메시지로 파일 정보 저장
                    ChatMessage fileMessage = ChatMessage.builder()
                            .consultationRoom(room)
                            .sender(sender)
                            .senderType(sender.getRole().equals("vet") ? 
                                    ChatMessage.SenderType.VET.name() : 
                                    ChatMessage.SenderType.USER.name())
                            .messageType(determineFileType(originalFileName))
                            .content("파일 첨부: " + originalFileName)
                            .fileUrl("/upload/" + subPath + "/" + storedFileName)
                            .fileName(originalFileName)
                            .fileSize(file.getSize())
                            .build();
                    
                    chatMessageRepository.save(fileMessage);
                    
                } catch (IOException e) {
                    log.error("파일 업로드 실패: {}", e.getMessage());
                    throw new RuntimeException("파일 업로드에 실패했습니다.");
                }
            }
        }
    }
    
    // 파일 타입 결정
    private String determineFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (extension.matches("jpg|jpeg|png|gif|bmp")) {
            return ChatMessage.MessageType.IMAGE.name();
        }
        return ChatMessage.MessageType.FILE.name();
    }
    
    // Entity를 Response DTO로 변환
    private QnaConsultationDto.Response convertToResponse(ConsultationRoom room) {
        // 메시지 조회
        List<ChatMessage> messages = chatMessageRepository
                .findByConsultationRoomOrderByCreatedAt(room);
        
        // 답변 존재 여부 확인
        boolean hasAnswer = messages.stream()
                .anyMatch(msg -> ChatMessage.SenderType.VET.name().equals(msg.getSenderType()));
        
        return QnaConsultationDto.Response.builder()
                .roomId(room.getRoomId())
                .roomUuid(room.getRoomUuid())
                .userId(room.getUser().getUserId())
                .userName(room.getUser().getUserName())
                .vetId(room.getVet().getVetId())
                .vetName(room.getVet().getName())
                .vetSpecialty(room.getVet().getSpecialization())
                .petId(room.getPet().getPetId())
                .petName(room.getPet().getPetName())
                .petSpecies(room.getPet().getAnimalType())  // 품종
                .petGender(room.getPet().getGender())      // 성별  
                .petAge(room.getPet().getAge())            // 나이
                .roomStatus(room.getRoomStatus())
                .title(room.getChiefComplaint()) // 제목으로 사용
                .category(room.getConsultationNotes() != null ? room.getConsultationNotes() : "건강") // consultationNotes에서 카테고리 가져오기
                .consultationFee(room.getConsultationFee())
                .paymentStatus(room.getPaymentStatus())
                .createdAt(room.getCreatedAt() != null ? room.getCreatedAt() : LocalDateTime.now())
                .updatedAt(room.getUpdatedAt() != null ? room.getUpdatedAt() : room.getCreatedAt())
                .messages(convertMessages(messages))
                .hasAnswer(hasAnswer)
                .totalMessages(messages.size())
                .hasReview(room.getReview() != null)
                .build();
    }
    
    // 메시지 리스트를 DTO로 변환
    private List<QnaConsultationDto.MessageDto> convertMessages(List<ChatMessage> messages) {
        return messages.stream()
                .map(msg -> QnaConsultationDto.MessageDto.builder()
                        .messageId(msg.getMessageId())
                        .senderType(msg.getSenderType())
                        .senderName(msg.getSender().getUserName())
                        .messageType(msg.getMessageType())
                        .content(msg.getContent())
                        .fileUrl(msg.getFileUrl())
                        .fileName(msg.getFileName())
                        .fileSize(msg.getFileSize())
                        .isRead(msg.isRead())
                        .isMainQuestion(isMainQuestion(msg))
                        .isMainAnswer(isMainAnswer(msg))
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    
    // 주 질문 여부 판단
    private boolean isMainQuestion(ChatMessage message) {
        return ChatMessage.SenderType.USER.name().equals(message.getSenderType()) 
                && "Y".equals(message.getIsImportant());
    }
    
    // 주 답변 여부 판단
    private boolean isMainAnswer(ChatMessage message) {
        return ChatMessage.SenderType.VET.name().equals(message.getSenderType()) 
                && "Y".equals(message.getIsImportant());
    }
    
    // 카테고리 추출
    private String extractCategory(List<ChatMessage> messages) {
        // ConsultationRoom의 consultationNotes에서 카테고리 정보를 가져옴
        // (Q&A 생성 시 카테고리를 consultationNotes에 임시 저장)
        return "건강"; // 기본값
    }
}