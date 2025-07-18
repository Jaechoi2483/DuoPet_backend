package com.petlogue.duopetbackend.user.model.service;

import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class SmsService {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.sender}")
    private String senderPhone;

    private DefaultMessageService messageService;

    // 전화번호 → 인증정보 저장
    private final Map<String, AuthCodeInfo> authCodeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    public void sendAuthCode(String phoneNumber) throws NurigoEmptyResponseException, NurigoUnknownException {
        String authCode = createRandomCode();
        log.info("발송 대상: {}, 인증번호: {}", phoneNumber, authCode);

        Message message = new Message();
        message.setFrom(senderPhone);
        message.setTo(phoneNumber);
        message.setText("[DuoPet] 인증번호 [" + authCode + "] 를 입력해주세요. (3분 내 유효)");

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException e) {
            log.error("SMS 전송 실패: {}", e.getMessage());
            throw new RuntimeException("SMS 전송 실패");
        }

        // 인증번호 저장 (3분 후 만료)
        authCodeMap.put(phoneNumber, new AuthCodeInfo(authCode, System.currentTimeMillis() + (3 * 60 * 1000)));
    }

    public boolean verifyAuthCode(String phoneNumber, String inputCode) {
        AuthCodeInfo info = authCodeMap.get(phoneNumber);

        if (info == null) return false;
        if (System.currentTimeMillis() > info.expireTime) {
            authCodeMap.remove(phoneNumber);
            return false;
        }

        boolean matched = info.code.equals(inputCode);
        if (matched) authCodeMap.remove(phoneNumber); // 사용 후 삭제
        return matched;
    }

    private String createRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private static class AuthCodeInfo {
        String code;
        long expireTime;

        public AuthCodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}