package com.petlogue.duopetbackend.info.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 50)
    private String loginId;

    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    @Column(name = "nickname", length = 50, nullable = false)
    private String hospitalName; // USERS.NICKNAME -> 병원 이름

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "user_email", length = 300, nullable = false)
    private String email;

    @Column(name = "role", length = 50, nullable = false)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "rename_filename", length = 255)
    private String renameFilename;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    // 추가 필드들 (실제 테이블에 없지만 프론트엔드에서 필요한 정보)
    @Transient
    private BigDecimal latitude;

    @Transient
    private BigDecimal longitude;

    @Transient
    private String openHours;

    @Transient
    private Boolean isEmergency;

    @Transient
    private String services;

    @Transient
    private BigDecimal rating;

    @Transient
    private Integer reviewCount;

    @Transient
    private String description;

    @Transient
    private String specialization;

    // 기본값 설정 메서드
    @PostLoad
    protected void setDefaults() {
        if (this.rating == null) {
            this.rating = new BigDecimal("4.5"); // 기본 평점
        }
        if (this.reviewCount == null) {
            this.reviewCount = 0; // 기본 리뷰 수
        }
        if (this.openHours == null) {
            this.openHours = "09:00 - 18:00"; // 기본 운영시간
        }
        if (this.isEmergency == null) {
            this.isEmergency = false; // 기본값: 응급병원 아님
        }
        if (this.services == null) {
            this.services = "진료,건강검진,예방접종"; // 기본 서비스
        }
        if (this.specialization == null) {
            this.specialization = "종합진료"; // 기본 전문분야
        }
        if (this.description == null) {
            this.description = "반려동물의 건강을 책임지는 " + this.hospitalName + "입니다.";
        }
        
        // 주소 기반 좌표 설정
        if (this.latitude == null || this.longitude == null) {
            setCoordinatesByAddress();
        }
    }
    
    private void setCoordinatesByAddress() {
        if (this.address == null) return;
        
        // 서울 구별
        if (address.contains("강남구")) {
            this.latitude = new BigDecimal("37.5172");
            this.longitude = new BigDecimal("127.0473");
        } else if (address.contains("서초구")) {
            this.latitude = new BigDecimal("37.4837");
            this.longitude = new BigDecimal("127.0324");
        } else if (address.contains("중랑구")) {
            this.latitude = new BigDecimal("37.6066");
            this.longitude = new BigDecimal("127.0925");
        } else if (address.contains("마포구")) {
            this.latitude = new BigDecimal("37.5638");
            this.longitude = new BigDecimal("126.9084");
        } else if (address.contains("강동구")) {
            this.latitude = new BigDecimal("37.5301");
            this.longitude = new BigDecimal("127.1238");
        } else if (address.contains("동대문구")) {
            this.latitude = new BigDecimal("37.5744");
            this.longitude = new BigDecimal("127.0395");
        } else if (address.contains("송파구")) {
            this.latitude = new BigDecimal("37.5145");
            this.longitude = new BigDecimal("127.1059");
        } else if (address.contains("용산구")) {
            this.latitude = new BigDecimal("37.5326");
            this.longitude = new BigDecimal("126.9910");
        } else if (address.contains("강서구")) {
            this.latitude = new BigDecimal("37.5509");
            this.longitude = new BigDecimal("126.8495");
        } else if (address.contains("성동구")) {
            this.latitude = new BigDecimal("37.5634");
            this.longitude = new BigDecimal("127.0371");
        } else if (address.contains("은평구")) {
            this.latitude = new BigDecimal("37.6027");
            this.longitude = new BigDecimal("126.9295");
        } else if (address.contains("종로구")) {
            this.latitude = new BigDecimal("37.5735");
            this.longitude = new BigDecimal("126.9789");
        } else if (address.contains("중구")) {
            this.latitude = new BigDecimal("37.5641");
            this.longitude = new BigDecimal("126.9979");
        } else if (address.contains("영등포구")) {
            this.latitude = new BigDecimal("37.5263");
            this.longitude = new BigDecimal("126.8962");
        } else if (address.contains("성북구")) {
            this.latitude = new BigDecimal("37.5894");
            this.longitude = new BigDecimal("127.0167");
        } else if (address.contains("노원구")) {
            this.latitude = new BigDecimal("37.6542");
            this.longitude = new BigDecimal("127.0568");
        } else if (address.contains("도봉구")) {
            this.latitude = new BigDecimal("37.6688");
            this.longitude = new BigDecimal("127.0471");
        } else if (address.contains("양천구")) {
            this.latitude = new BigDecimal("37.5170");
            this.longitude = new BigDecimal("126.8664");
        } else if (address.contains("구로구")) {
            this.latitude = new BigDecimal("37.4955");
            this.longitude = new BigDecimal("126.8874");
        } else if (address.contains("금천구")) {
            this.latitude = new BigDecimal("37.4569");
            this.longitude = new BigDecimal("126.8955");
        } else if (address.contains("동작구")) {
            this.latitude = new BigDecimal("37.5124");
            this.longitude = new BigDecimal("126.9392");
        } else if (address.contains("관악구")) {
            this.latitude = new BigDecimal("37.4781");
            this.longitude = new BigDecimal("126.9514");
        } else if (address.contains("서대문구")) {
            this.latitude = new BigDecimal("37.5791");
            this.longitude = new BigDecimal("126.9368");
        } else if (address.contains("광진구")) {
            this.latitude = new BigDecimal("37.5384");
            this.longitude = new BigDecimal("127.0823");
        }
        // 경기도
        else if (address.contains("고양시")) {
            this.latitude = new BigDecimal("37.6584");
            this.longitude = new BigDecimal("126.8320");
        } else if (address.contains("성남시")) {
            this.latitude = new BigDecimal("37.4449");
            this.longitude = new BigDecimal("127.1388");
        } else if (address.contains("수원시")) {
            this.latitude = new BigDecimal("37.2636");
            this.longitude = new BigDecimal("127.0286");
        } else if (address.contains("안양시")) {
            this.latitude = new BigDecimal("37.3943");
            this.longitude = new BigDecimal("126.9568");
        } else if (address.contains("부천시")) {
            this.latitude = new BigDecimal("37.5035");
            this.longitude = new BigDecimal("126.7660");
        } else if (address.contains("의왕시")) {
            this.latitude = new BigDecimal("37.3448");
            this.longitude = new BigDecimal("126.9686");
        } else if (address.contains("광명시")) {
            this.latitude = new BigDecimal("37.4784");
            this.longitude = new BigDecimal("126.8644");
        } else if (address.contains("안산시")) {
            this.latitude = new BigDecimal("37.3219");
            this.longitude = new BigDecimal("126.8309");
        } else if (address.contains("화성시")) {
            this.latitude = new BigDecimal("37.1996");
            this.longitude = new BigDecimal("126.8312");
        } else if (address.contains("평택시")) {
            this.latitude = new BigDecimal("36.9921");
            this.longitude = new BigDecimal("127.1130");
        } else if (address.contains("시흥시")) {
            this.latitude = new BigDecimal("37.3802");
            this.longitude = new BigDecimal("126.8028");
        } else if (address.contains("용인시")) {
            this.latitude = new BigDecimal("37.2411");
            this.longitude = new BigDecimal("127.1776");
        } else if (address.contains("파주시")) {
            this.latitude = new BigDecimal("37.7599");
            this.longitude = new BigDecimal("126.7802");
        } else if (address.contains("김포시")) {
            this.latitude = new BigDecimal("37.6151");
            this.longitude = new BigDecimal("126.7156");
        } else if (address.contains("남양주시")) {
            this.latitude = new BigDecimal("37.6360");
            this.longitude = new BigDecimal("127.2169");
        } else if (address.contains("의정부시")) {
            this.latitude = new BigDecimal("37.7380");
            this.longitude = new BigDecimal("127.0339");
        } else if (address.contains("구리시")) {
            this.latitude = new BigDecimal("37.5943");
            this.longitude = new BigDecimal("127.1296");
        } else if (address.contains("하남시")) {
            this.latitude = new BigDecimal("37.5393");
            this.longitude = new BigDecimal("127.2147");
        } else if (address.contains("오산시")) {
            this.latitude = new BigDecimal("37.1498");
            this.longitude = new BigDecimal("127.0772");
        } else if (address.contains("군포시")) {
            this.latitude = new BigDecimal("37.3617");
            this.longitude = new BigDecimal("126.9351");
        } else if (address.contains("과천시")) {
            this.latitude = new BigDecimal("37.4292");
            this.longitude = new BigDecimal("126.9875");
        }
        // 광역시
        else if (address.contains("부산") || address.contains("부산광역시")) {
            this.latitude = new BigDecimal("35.1796");
            this.longitude = new BigDecimal("129.0756");
        } else if (address.contains("대구") || address.contains("대구광역시")) {
            this.latitude = new BigDecimal("35.8714");
            this.longitude = new BigDecimal("128.6014");
        } else if (address.contains("인천") || address.contains("인천광역시")) {
            this.latitude = new BigDecimal("37.4563");
            this.longitude = new BigDecimal("126.7052");
        } else if (address.contains("광주") || address.contains("광주광역시")) {
            this.latitude = new BigDecimal("35.1595");
            this.longitude = new BigDecimal("126.8526");
        } else if (address.contains("대전") || address.contains("대전광역시")) {
            this.latitude = new BigDecimal("36.3504");
            this.longitude = new BigDecimal("127.3845");
        } else if (address.contains("울산") || address.contains("울산광역시")) {
            this.latitude = new BigDecimal("35.5384");
            this.longitude = new BigDecimal("129.3114");
        } else if (address.contains("세종") || address.contains("세종시")) {
            this.latitude = new BigDecimal("36.4800");
            this.longitude = new BigDecimal("127.2890");
        }
        // 제주도
        else if (address.contains("제주") || address.contains("제주시")) {
            this.latitude = new BigDecimal("33.4996");
            this.longitude = new BigDecimal("126.5312");
        } else if (address.contains("서귀포")) {
            this.latitude = new BigDecimal("33.2541");
            this.longitude = new BigDecimal("126.5600");
        }
        // 기타 지역
        else if (address.contains("전주") || address.contains("전주시")) {
            this.latitude = new BigDecimal("35.8242");
            this.longitude = new BigDecimal("127.1480");
        } else if (address.contains("청주") || address.contains("청주시")) {
            this.latitude = new BigDecimal("36.6424");
            this.longitude = new BigDecimal("127.4890");
        } else if (address.contains("춘천") || address.contains("춘천시")) {
            this.latitude = new BigDecimal("37.8813");
            this.longitude = new BigDecimal("127.7298");
        } else if (address.contains("원주") || address.contains("원주시")) {
            this.latitude = new BigDecimal("37.3422");
            this.longitude = new BigDecimal("127.9202");
        } else if (address.contains("포항") || address.contains("포항시")) {
            this.latitude = new BigDecimal("36.0190");
            this.longitude = new BigDecimal("129.3435");
        } else if (address.contains("창원") || address.contains("창원시")) {
            this.latitude = new BigDecimal("35.2280");
            this.longitude = new BigDecimal("128.6811");
        } else if (address.contains("목포") || address.contains("목포시")) {
            this.latitude = new BigDecimal("34.8118");
            this.longitude = new BigDecimal("126.3922");
        } else if (address.contains("여수") || address.contains("여수시")) {
            this.latitude = new BigDecimal("34.7604");
            this.longitude = new BigDecimal("127.6622");
        } else if (address.contains("천안") || address.contains("천안시")) {
            this.latitude = new BigDecimal("36.8151");
            this.longitude = new BigDecimal("127.1139");
        }
        // 기본값: 서울 중심부 
        else {
            this.latitude = new BigDecimal("37.5665");
            this.longitude = new BigDecimal("126.9780");
        }
    }
}