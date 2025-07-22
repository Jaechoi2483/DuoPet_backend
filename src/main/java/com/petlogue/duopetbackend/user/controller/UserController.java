package com.petlogue.duopetbackend.user.controller;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import com.petlogue.duopetbackend.user.jpa.entity.UserEntity;
import com.petlogue.duopetbackend.user.model.dto.SmsRequestDto;
import com.petlogue.duopetbackend.user.model.dto.UserDto;
import com.petlogue.duopetbackend.user.model.service.UserService;
import com.petlogue.duopetbackend.user.jpa.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    /**
     * 회원가입 1단계 - 아이디, 비밀번호 입력 처리
     */
    @PostMapping("/signup/step1")
    public ResponseEntity<String> signup(@RequestBody UserDto userDto) {
        userService.signup(userDto);
        return ResponseEntity.ok("회원가입 1단계 완료");
    }

    /**
     * 회원가입 2단계 - 개인정보 입력 + 프로필 이미지 업로드
     */
    @PostMapping("/signup/step2")
    public ResponseEntity<?> signupStep2(
            @RequestPart("data") UserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            UserDto processed = userService.processStep2(userDto, file);
            return ResponseEntity.ok(processed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 회원가입 4단계 - 최종 회원 등록 처리
     * - 프로필 이미지 포함
     * - 전문가/보호소일 경우 각 Service 호출
     */
    @PostMapping("/signup/final")
    public ResponseEntity<?> signupFinal(
            @RequestPart("data") UserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            UserEntity savedUser = userService.saveFinalUser(userDto, file);

            // 응답 객체 구성
            Map<String, Object> response = new HashMap<>();
            response.put("userId", savedUser.getUserId());         // 프론트에서 필요한 값
            response.put("nickname", savedUser.getNickname());     // 선택적으로 추가
            response.put("message", "회원가입이 완료되었습니다.");

            return ResponseEntity.ok(response); // 구조화된 응답으로 프론트 연결
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("회원가입 실패: " + e.getMessage());
        }
    }

    /**
     * 아이디 중복 확인
     */
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId);
        return ResponseEntity.ok(exists); // true = 중복 O
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname,
                                                 @RequestParam(required = false) Long userId) {
        boolean exists;

        if (userId != null) {
            // 자기 자신의 닉네임인지 확인
            UserEntity currentUser = userRepository.findByUserId(userId);
            if (currentUser != null && nickname.equals(currentUser.getNickname())) {
                exists = false; // 자기 닉네임은 중복 아님
            } else {
                exists = userRepository.existsByNickname(nickname);
            }
        } else {
            exists = userRepository.existsByNickname(nickname);
        }

        return ResponseEntity.ok(exists);
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String userEmail) {
        boolean exists = userRepository.existsByUserEmail(userEmail);
        return ResponseEntity.ok(exists); // true = 중복 O
    }

    // 소셜 로그인 후, 사용자 정보 업데이트 API
    @PutMapping("/social-update")
    public ResponseEntity<String> updateUserInfo(@RequestBody UserDto userDto,
                                                 @RequestHeader("Authorization") String authHeader,
                                                 HttpServletRequest request) {  // HttpServletRequest 추가
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String token = authHeader.replace("Bearer ", "");

            // JWT에서 userId 추출
            Long userId = jwtUtil.getUserIdFromRequest(request);  // 이제 request에서 userId를 추출

            // 사용자 정보 업데이트
            userService.updateUserInfo(userId, userDto);

            return ResponseEntity.ok("회원 정보가 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("업데이트 중 오류 발생", e);  // 예외 로그 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정보 업데이트에 실패했습니다.");
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserEntity> getUserByUserId(@PathVariable Long userId) {
        // userNo로 사용자 정보 조회
        UserEntity user = userRepository.findByUserId(userId);

        if (user != null) {
            return ResponseEntity.ok(user);  // 사용자 정보 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // 사용자 없음
        }
    }

    /**
     * 현재 로그인한 사용자의 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            // JWT에서 userId 추출
            Long userId = jwtUtil.getUserIdFromRequest(request);
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
            }
            
            // 사용자 정보 조회
            UserEntity user = userRepository.findByUserId(userId);
            
            if (user != null) {
                // 민감한 정보는 제외하고 DTO로 변환하여 반환
                UserDto userDto = user.toDto();
                userDto.setUserPwd(null); // 비밀번호는 제거
                return ResponseEntity.ok(userDto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 조회에 실패했습니다.");
        }
    }

    /**
     * 현재 로그인한 사용자의 정보 업데이트 (프로필 이미지 포함)
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            @RequestPart("data") UserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        try {
            // JWT에서 userId 추출
            Long userId = jwtUtil.getUserIdFromRequest(request);
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
            }
            
            // 사용자 정보 업데이트
            UserEntity updatedUser = userService.updateUserProfile(userId, userDto, file);
            
            if (updatedUser != null) {
                UserDto responseDto = updatedUser.toDto();
                responseDto.setUserPwd(null); // 비밀번호는 제거
                return ResponseEntity.ok(responseDto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("사용자 정보 업데이트 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 업데이트에 실패했습니다.");
        }
    }

    /**
     * [아이디 찾기] 이름 + 전화번호로 loginId 반환
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody SmsRequestDto dto) {
        try {
            String userName = dto.getUserName();
            String phone = dto.getPhone();

            // 입력값 유효성 검사
            if (userName == null || phone == null || userName.trim().isEmpty() || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("이름과 전화번호를 모두 입력해주세요.");
            }

            String loginId = userService.findLoginIdByNameAndPhone(userName, phone);
            return ResponseEntity.ok().body(Map.of("loginId", loginId));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 아이디가 없습니다.");
        }
    }

    /**
     * [비밀번호 찾기] loginId + 전화번호 일치 여부 확인
     */
    @PostMapping("/check-user")
    public ResponseEntity<?> checkUserExists(@RequestBody SmsRequestDto dto) {
        boolean exists = userService.existsByLoginIdAndPhone(dto.getLoginId(), dto.getPhone());

        if (exists) {
            return ResponseEntity.ok(Map.of("userExists", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "일치하는 계정을 찾을 수 없습니다."));
        }
    }

    /**
     * 비밀번호 재설정
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String loginId = request.get("loginId");
        String newPassword = request.get("newPassword");

        try {
            userService.resetPassword(loginId, newPassword);
            return ResponseEntity.ok("비밀번호 변경 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호 변경 실패: " + e.getMessage());
        }
    }

    /**
     * 얼굴 이미지 저장 처리 (촬영 이미지 등록)
     */
    @PostMapping("/face-upload")
    public ResponseEntity<?> uploadFaceImage(@RequestParam("userId") Long userId,
                                             @RequestParam("faceImage") MultipartFile faceImage) {
        try {
            // 1. 얼굴 이미지 저장
            String savedFileName = userService.saveFaceImageByLoginId(userId, faceImage);

            // 2. FastAPI 호출 (임베딩 저장)
            String filePath = "C:/upload_files/face/" + savedFileName;

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("user_id", userId.toString());
            body.add("image", new FileSystemResource(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            String fastApiUrl = "http://localhost:8000/api/v1/face-login/register";
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, requestEntity, String.class);

            System.out.println("FastAPI 응답: " + response.getBody());

            return ResponseEntity.ok(Map.of(
                    "message", "얼굴 이미지 저장 및 등록 완료",
                    "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "저장 실패", "detail", e.getMessage()));
        }
    }


    /**
     * 얼굴 등록 여부 확인 API
     * 요청: GET /users/check-face?userId=123
     * 응답: { "userId": 123, "faceRegistered": true }
     */
    @GetMapping("/check-face")
    public ResponseEntity<Map<String, Object>> checkFaceRegistered(@RequestParam Long userId) {
        log.info("얼굴 등록 여부 요청 수신: userId = {}", userId);

        boolean isRegistered = userService.isFaceRegistered(userId);
        log.info("사용자 [{}] 얼굴 등록 여부: {}", userId, isRegistered);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("faceRegistered", isRegistered);
        return ResponseEntity.ok(body);
    }
}
