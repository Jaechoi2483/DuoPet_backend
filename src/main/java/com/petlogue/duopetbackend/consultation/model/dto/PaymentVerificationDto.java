package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerificationDto {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Integer totalAmount;
    private String method;
    private String status;
    private LocalDateTime approvedAt;
    private String card;
    private String receiptUrl;
    private String checkoutUrl;
    private String failureCode;
    private String failureMessage;
}