package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.ShelterRepository;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.ShelterDto;
import com.petlogue.duopetbackend.user.model.service.ShelterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shelter")
public class ShelterController {

    private final ShelterService shelterService;
    private final UserRepository userRepository;
    private final ShelterRepository shelterRepository;

    public ShelterController(@Qualifier("shelterUserService") ShelterService shelterService, 
                           UserRepository userRepository, 
                           @Qualifier("shelterUserRepository") ShelterRepository shelterRepository) {
        this.shelterService = shelterService;
        this.userRepository = userRepository;
        this.shelterRepository = shelterRepository;
    }

    /**
     * 보호소 등록
     */
    @PostMapping("/register")
    public String registerShelter(
            @RequestBody ShelterDto shelterDto,
            @RequestParam("loginId") String loginId
    ) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        shelterService.registerShelter(user, shelterDto);

        return "보호소 등록 완료 (승인 대기 중)";
    }

    /**
     * 보호소 등록 여부 확인
     */
    @GetMapping("/check")
    public boolean isAlreadyRegistered(@RequestParam("loginId") String loginId) {
        UserEntity user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return shelterRepository.existsByUser(user);
    }
}
