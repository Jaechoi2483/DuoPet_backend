package com.petlogue.duopetbackend.user.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRequestDto {
    private String phone;
    private String authCode;
}