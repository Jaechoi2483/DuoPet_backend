package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.jpa.entity.Payment;
import com.petlogue.duopetbackend.consultation.jpa.repository.PaymentRepository;
import com.petlogue.duopetbackend.consultation.model.dto.PaymentVerificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${toss.payments.secret-key}")
    private String secretKey;
    
    @Value("${toss.payments.api-base-url}")
    private String tossApiBaseUrl;
    
    /**
     * 토스페이먼츠 결제 검증
     */
    @Transactional
    public Payment verifyPayment(String paymentKey, String orderId, Integer amount) {
        
        log.info("결제 검증 시작 - paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);
        
        try {
            // 토스페이먼츠 API 호출하여 결제 정보 확인
            String credentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());
            
            WebClient webClient = webClientBuilder
                .baseUrl(tossApiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
            
            PaymentVerificationDto verification = webClient.get()
                .uri(paymentKey)
                .retrieve()
                .bodyToMono(PaymentVerificationDto.class)
                .block();
            
            if (verification == null) {
                throw new RuntimeException("결제 정보를 확인할 수 없습니다.");
            }
            
            log.info("토스페이먼츠 응답: {}", verification);
            
            // 금액 검증
            if (!verification.getTotalAmount().equals(amount)) {
                throw new RuntimeException("결제 금액이 일치하지 않습니다. 요청: " + amount + ", 실제: " + verification.getTotalAmount());
            }
            
            // 주문 ID 검증
            if (!verification.getOrderId().equals(orderId)) {
                throw new RuntimeException("주문 번호가 일치하지 않습니다.");
            }
            
            // 결제 상태 확인
            if (!"DONE".equals(verification.getStatus())) {
                throw new RuntimeException("결제가 완료되지 않았습니다. 상태: " + verification.getStatus());
            }
            
            // 결제 정보 저장
            Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentKey(paymentKey)
                .amount(amount)
                .method(verification.getMethod())
                .status(Payment.PaymentStatus.COMPLETED)
                .approvedAt(verification.getApprovedAt())
                .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            log.info("결제 정보 저장 완료 - paymentId: {}", savedPayment.getPaymentId());
            
            return savedPayment;
            
        } catch (Exception e) {
            log.error("결제 검증 실패", e);
            throw new RuntimeException("결제 검증에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 결제 취소
     */
    @Transactional
    public Payment cancelPayment(String paymentKey, String cancelReason) {
        
        log.info("결제 취소 시작 - paymentKey: {}, reason: {}", paymentKey, cancelReason);
        
        try {
            Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
            
            // 토스페이먼츠 API 호출하여 결제 취소
            String credentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());
            
            WebClient webClient = webClientBuilder
                .baseUrl(tossApiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
            
            String requestBody = "{\"cancelReason\":\"" + cancelReason + "\"}";
            
            String response = webClient.post()
                .uri(paymentKey + "/cancel")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            log.info("결제 취소 응답: {}", response);
            
            // 결제 상태 업데이트
            payment.setStatus(Payment.PaymentStatus.CANCELLED);
            payment.setCancelReason(cancelReason);
            payment.setCancelledAt(LocalDateTime.now());
            
            Payment updatedPayment = paymentRepository.save(payment);
            log.info("결제 취소 완료 - paymentId: {}", updatedPayment.getPaymentId());
            
            return updatedPayment;
            
        } catch (Exception e) {
            log.error("결제 취소 실패", e);
            throw new RuntimeException("결제 취소에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 결제 정보 조회
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다. orderId: " + orderId));
    }
    
    /**
     * 상담방 ID로 결제 정보 조회
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByConsultationRoomId(Long roomId) {
        return paymentRepository.findByConsultationRoomRoomId(roomId)
            .orElse(null);
    }
}