package com.petlogue.duopetbackend.consultation.jpa.repository;

import com.petlogue.duopetbackend.consultation.jpa.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByConsultationRoomRoomId(Long roomId);
}