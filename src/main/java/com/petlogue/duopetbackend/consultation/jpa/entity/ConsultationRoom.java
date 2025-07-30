package com.petlogue.duopetbackend.consultation.jpa.entity;

import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "consultation_room")
@Getter
@Slf4j
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "vet", "pet", "schedule", "messages", "review"})
public class ConsultationRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;
    
    @Column(name = "room_uuid", length = 36, nullable = false, unique = true)
    @Builder.Default
    private String roomUuid = UUID.randomUUID().toString();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private VetEntity vet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private PetEntity pet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private VetSchedule schedule;
    
    @Column(name = "room_status", length = 20, nullable = false)
    @Builder.Default
    private String roomStatus = "CREATED";
    
    @Column(name = "consultation_type", length = 20, nullable = false)
    @Builder.Default
    private String consultationType = "CHAT";
    
    @Column(name = "scheduled_datetime")
    private LocalDateTime scheduledDatetime;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;
    
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private String paymentStatus = "PENDING";
    
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "chief_complaint", columnDefinition = "CLOB")
    @Lob
    private String chiefComplaint;
    
    @Column(name = "consultation_notes", columnDefinition = "CLOB")
    @Lob
    private String consultationNotes;
    
    @Column(name = "prescription", columnDefinition = "CLOB")
    @Lob
    private String prescription;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // JPA 이벤트 - 생성 시 명시적으로 시간 설정
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
            log.info("ConsultationRoom 생성 시간 설정: {}", this.createdAt);
        }
    }
    
    // 연관관계
    @OneToMany(mappedBy = "consultationRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();
    
    @OneToOne(mappedBy = "consultationRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConsultationReview review;
    
    @OneToOne(mappedBy = "consultationRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
    
    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = false;
    
    // 상태 상수
    public enum RoomStatus {
        CREATED,       // 생성됨
        WAITING,       // 대기중
        IN_PROGRESS,   // 진행중
        COMPLETED,     // 완료
        CANCELLED,     // 취소
        NO_SHOW,       // 노쇼
        REJECTED,      // 거절됨
        TIMED_OUT,     // 시간 초과
        PENDING,       // 대기중 (Q&A용)
        ANSWERED       // 답변완료 (Q&A용)
    }
    
    public enum ConsultationType {
        CHAT, VIDEO, PHONE, QNA  // QNA 추가
    }
    
    public enum PaymentStatus {
        PENDING, PAID, REFUNDED, FAILED
    }
    
    // 편의 메서드
    public void startConsultation() {
        this.roomStatus = RoomStatus.IN_PROGRESS.name();
        this.startedAt = LocalDateTime.now();
    }
    
    public void endConsultation() {
        this.roomStatus = RoomStatus.COMPLETED.name();
        this.endedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.durationMinutes = (int) java.time.Duration.between(this.startedAt, this.endedAt).toMinutes();
        }
    }
    
    public void cancelConsultation() {
        this.roomStatus = RoomStatus.CANCELLED.name();
    }
    
    public void markAsPaid(String paymentMethod) {
        this.paymentStatus = PaymentStatus.PAID.name();
        this.paymentMethod = paymentMethod;
        this.paidAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return RoomStatus.IN_PROGRESS.name().equals(this.roomStatus);
    }
    
    public boolean isPaid() {
        return PaymentStatus.PAID.name().equals(this.paymentStatus);
    }
    
    public Boolean getIsPaid() {
        return this.isPaid;
    }
    
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        message.setConsultationRoom(this);
    }
    
    // Q&A 답변 완료 처리
    public void completeAnswer() {
        if ("QNA".equals(this.consultationType)) {
            this.roomStatus = RoomStatus.ANSWERED.name();
        }
    }
}