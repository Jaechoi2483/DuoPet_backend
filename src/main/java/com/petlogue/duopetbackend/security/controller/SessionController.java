package com.petlogue.duopetbackend.security.controller;

import com.petlogue.duopetbackend.security.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
public class SessionController {

    private final JWTUtil jwtUtil;

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = header.substring(7);

        if (jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long now = System.currentTimeMillis();
        long exp = jwtUtil.getExpiration(token).getTime();
        long remaining = Math.max(0, exp - now);

        boolean showPopup = remaining <= 5 * 60 * 1000;

        Map<String, Object> result = new HashMap<>();
        result.put("remainingTimeMs", remaining);
        result.put("showExtendPopup", showPopup);

        return ResponseEntity.ok(result);
    }
}
