package com.petlogue.duopetbackend.security.jwt.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="REFRESH_TOKEN")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "refresh_token", nullable = false, length = 512)
    private String refreshToken;
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    @Column(name = "device_info", length = 255)
    private String deviceInfo;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "expires_at")
    private java.util.Date expiresAt;
    @Column(name = "token_status", length = 20)
    private String tokenStatus;

    public void updateToken(String refreshToken, String ipAddress, String deviceInfo, java.util.Date expiresAt) {
        this.refreshToken = refreshToken;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now(); // 갱신 시각 업데이트
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.tokenStatus == null) {
            this.tokenStatus = "ACTIVE";
        }
    }
}