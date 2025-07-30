package com.petlogue.duopetbackend.consultation.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfoDto {
    private String paymentKey;
    private String orderId;
    private Integer amount;
    private String paymentMethod;
    private String status;
}