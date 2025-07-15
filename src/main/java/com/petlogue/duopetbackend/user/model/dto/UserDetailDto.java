package com.petlogue.duopetbackend.user.model.dto;

import com.petlogue.duopetbackend.user.jpa.entity.ShelterEntity;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import lombok.Getter;


@Getter
public class UserDetailDto {
    private final UserEntity user;
    private final VetEntity vetProfile;
    private final ShelterEntity shelterProfile; // <-- 실제 클래스 타입으로 수정

    // JPQL이 이 생성자를 호출하여 객체를 생성합니다.
    public UserDetailDto(UserEntity user, VetEntity vetProfile, ShelterEntity shelterProfile) { // <-- 파라미터도 실제 클래스 타입으로 수정
        this.user = user;
        this.vetProfile = vetProfile;
        this.shelterProfile = shelterProfile;
    }


}
