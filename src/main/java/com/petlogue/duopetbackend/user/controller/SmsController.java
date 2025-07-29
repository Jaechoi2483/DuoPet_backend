package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.model.dto.SmsRequestDto;
import com.petlogue.duopetbackend.user.model.service.SmsService;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;
    private final UserRepository userRepository;

    /**
     * 인증번호 전송 (아이디 찾기 or 비밀번호 찾기)
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendSmsCode(@RequestBody SmsRequestDto dto)
            throws NurigoEmptyResponseException, NurigoUnknownException {

        String phone = dto.getPhone();

        if (dto.getLoginId() != null) {
            // 비밀번호 찾기
            Optional<UserEntity> userOpt = userRepository.findByLoginIdAndPhoneWithoutHyphen(dto.getLoginId(), phone.replaceAll("-", ""));
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("아이디 또는 전화번호가 일치하지 않습니다.");
            }
            log.info("[비밀번호 찾기] loginId={}, phone={}", dto.getLoginId(), phone);
            smsService.sendAuthCode(phone);
            return ResponseEntity.ok("인증번호 전송 완료");

        } else if (dto.getUserName() != null) {
            // 아이디 찾기
            Optional<UserEntity> userOpt = userRepository.findByUserNameAndPhoneWithoutHyphen(dto.getUserName(), phone.replaceAll("-", ""));
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("이름 또는 전화번호가 일치하지 않습니다.");
            }
            log.info("[아이디 찾기] name={}, phone={}", dto.getUserName(), phone);
            smsService.sendAuthCode(phone);
            return ResponseEntity.ok("인증번호 전송 완료");

        } else {
            // 회원가입 인증
            log.info("[회원가입 인증] phone={}", phone);
            boolean exists = userRepository.existsByPhoneAndProviderIsNull(phone.replaceAll("-", ""));
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 가입된 전화번호입니다.");
            }
            smsService.sendAuthCode(phone);
            return ResponseEntity.ok("인증번호 전송 완료");
        }
    }

    /**
     * 인증번호 확인
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyCode(@RequestBody SmsRequestDto dto) {
        log.info("👉 인증 요청: phone={}, code={}", dto.getPhone(), dto.getAuthCode());

        boolean result = smsService.verifyAuthCode(dto.getPhone(), dto.getAuthCode());

        if (result) {
            log.info("인증 성공: phone={}", dto.getPhone());
        } else {
            log.warn("인증 실패: phone={}, 입력코드={}", dto.getPhone(), dto.getAuthCode());
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("verified", result);
        return result ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(400).body(response);
    }
}
