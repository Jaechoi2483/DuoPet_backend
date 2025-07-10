package com.petlogue.duopetbackend.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "user_pwd", nullable = false, length = 200)
    private String userPwd;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "user_name", nullable = false, unique = true, length = 50)
    private String userName;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender", nullable = false, length = 1)
    private String gender;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "user_email", nullable = false, length = 300)
    private String userEmail;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "rename_filename", length = 255)
    private String renameFilename;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "face_recognition_id", unique = true, length = 255)
    private String faceRecognitionId;
}
