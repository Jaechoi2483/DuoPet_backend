package com.petlogue.duopetbackend.security.jwt;

import com.petlogue.duopetbackend.user.model.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access_expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh_expiration}")
    private Long refreshExpiration;

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    /**
     * JWT 토큰 생성
     * @param userDto 사용자 정보 (id, nickname, role 등 포함)
     * @param category access / refresh
     */
    public String generateToken(UserDto userDto, String category) {
        return Jwts.builder()
                .setSubject(userDto.getLoginId())  // 토큰의 subject = 로그인 ID
                .claim("userNo", userDto.getUserId())
                .claim("category", category)       // access / refresh
                .claim("nickname", userDto.getNickname())
                .claim("role", userDto.getRole())  // "USER", "ADMIN" 등
                .setExpiration(new Date(
                        System.currentTimeMillis() +
                                (category.equals("access") ? accessExpiration : refreshExpiration)))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // 토큰에서 Claims 추출
    public Claims getClaimsFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.error("token is empty");
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token.trim())
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired but returning claims");
            return e.getClaims();
        } catch (Exception e) {
            log.error("JWT parsing error", e);
            throw e;
        }
    }

    // 수정 / 삭제시 사용자 ID 와 게시물 작성자 ID 일치 여부 확인
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // "Bearer " 제거
            Claims claims = getClaimsFromToken(token);

            try {
                Long userNo = Long.valueOf(claims.get("userNo").toString());
                log.info("JWTUtil - 추출된 userNo: {}", userNo);
                return Long.valueOf(claims.get("userNo").toString());
            } catch (Exception e) {
                log.error("userNo 추출 실패", e);
                return null;
            }
        }
        return null;
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }

    // subject (loginId) 추출
    public String getLoginIdFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // role 추출
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    // category 추출
    public String getCategoryFromToken(String token) {
        return getClaimsFromToken(token).get("category", String.class);
    }
}
