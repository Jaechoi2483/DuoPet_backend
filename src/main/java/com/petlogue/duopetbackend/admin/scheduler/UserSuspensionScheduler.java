package com.petlogue.duopetbackend.admin.scheduler; // 패키지 경로 수정

import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSuspensionScheduler {

    private final UserRepository userRepository;

    /**
     * 매시간 정각에 실행되어 정지 만료된 사용자를 확인하고 상태를 해제합니다.
     * cron = "0 0 * * * *" (초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void releaseSuspendedUsers() {
        log.info("정지 만료 사용자 확인 스케줄러 시작...");

        List<UserEntity> expiredUsers = userRepository.findExpiredSuspensions(LocalDateTime.now());

        if (expiredUsers.isEmpty()) {
            log.info("정지 만료된 사용자가 없습니다.");
            return;
        }

        for (UserEntity user : expiredUsers) {
            log.info("사용자 ID {} ({})의 정지를 해제합니다. 만료 시간: {}", user.getUserId(), user.getLoginId(), user.getSuspendedUntil());
            user.setStatus("active"); // DB에 소문자로 저장해야 하므로 'active'로 설정
            user.setSuspendedUntil(null); // 만료 시간 초기화
        }



        userRepository.saveAll(expiredUsers);
        log.info("총 {}명의 사용자의 정지를 해제했습니다.", expiredUsers.size());
    }
}