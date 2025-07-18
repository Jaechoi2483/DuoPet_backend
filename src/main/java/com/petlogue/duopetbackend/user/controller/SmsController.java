package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.user.model.dto.SmsRequestDto;
import com.petlogue.duopetbackend.user.model.service.SmsService;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    /**
     * 인증번호 전송
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendSmsCode(@RequestBody SmsRequestDto dto) throws NurigoEmptyResponseException, NurigoUnknownException {
        smsService.sendAuthCode(dto.getPhone());
        return ResponseEntity.ok("인증번호 전송 완료");
    }

    /**
     * 인증번호 확인 (선택사항)
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(
            @RequestParam String phone,
            @RequestParam String code
    ) {
        boolean result = smsService.verifyAuthCode(phone, code);
        if (result) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(400).body("인증 실패");
        }
    }
}
