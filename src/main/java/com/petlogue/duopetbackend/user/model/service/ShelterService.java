package com.petlogue.duopetbackend.user.model.service;

import com.petlogue.duopetbackend.user.jpa.entity.ShelterEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.ShelterRepository;
import com.petlogue.duopetbackend.user.model.dto.ShelterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("shelterUserService")
public class ShelterService {

    private final ShelterRepository shelterRepository;
    
    public ShelterService(@Qualifier("shelterUserRepository") ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    /**
     * 보호소 등록 처리
     * - 중복 등록 방지
     * - 파일 업로드는 현재 제외
     * - ShelterEntity 저장
     */
    public void registerShelter(UserEntity userEntity, ShelterDto shelterDto) {

        // 1. 중복 체크
        if (shelterRepository.existsByUser(userEntity)) {
            throw new IllegalStateException("이미 보호소로 등록된 사용자입니다.");
        }

        // 2. Entity 저장
        ShelterEntity shelter = shelterDto.toEntity(userEntity);
        shelterRepository.save(shelter);
    }
}
