package com.petlogue.duopetbackend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//샘플 데이터 비밀번호 암호화
public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "1234";
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("pwd: " + hashedPassword);
    }
}
