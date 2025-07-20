package com.petlogue.duopetbackend.user.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRequestDto {
    // 아이디 찾기 및 인증번호 전송용
    private String userName;       // 아이디 찾기 시 이름 확인
    private String phone;      // 인증번호 수신용 전화번호

    // 인증번호 확인용
    private String authCode;   // 사용자 입력 인증번호

    // 비밀번호 재설정용
    private String loginId;    // 인증 완료된 사용자 ID
    private String newPassword; // 새 비밀번호 (암호화 후 저장)
}