package com.petlogue.duopetbackend.mypage.model.dto;

import lombok.Data;

@Data
public class PasswordChangeRequestDto {
    private String currentPassword;
    private String newPassword;
}