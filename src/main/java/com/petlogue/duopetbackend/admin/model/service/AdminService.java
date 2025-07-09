package com.petlogue.duopetbackend.admin.model.service;


import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;

    public Page<UserDto> findAllUsers(Pageable pageable) {
        // 1. UserRepository를 사용해 DB에서 Page<UserEntity> 형태로 데이터를 조회합니다.
        Page<UserEntity> userEntityPage = userRepository.findAll(pageable);

        // 2. Page 객체의 map 기능을 사용해 Page<UserEntity>를 Page<UserDto>로 변환합니다.
        //    UserEntity::toDto는 각 UserEntity에 대해 toDto() 메소드를 호출하라는 의미입니다.
        return userEntityPage.map(UserEntity::toDto);
    }
}
