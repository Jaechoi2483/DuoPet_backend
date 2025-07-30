package com.petlogue.duopetbackend.consultation.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", insertable = false, updatable = false)
    private Long paymentId;
    
    @Column(nullable = false, unique = true)
    private String orderId;
    
    @Column(nullable = false)
    private String paymentKey;
    
    @Column(nullable = false)
    private Integer amount;
    
    @Column(nullable = false)
    private String method;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @OneToOne
    @JoinColumn(name = "consultation_room_id")
    private ConsultationRoom consultationRoom;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancel_reason")
    private String cancelReason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        FAILED
    }
}