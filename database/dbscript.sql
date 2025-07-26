SET DEFINE OFF;
/*==============================================================
  0.  기존 객체 정리 - 순서 무관, CASCADE CONSTRAINTS 사용
==============================================================*/
DROP TABLE bookmark                 CASCADE CONSTRAINTS;
DROP TABLE LIKE_TB                   CASCADE CONSTRAINTS;
DROP TABLE faq                      CASCADE CONSTRAINTS;
DROP TABLE comments                 CASCADE CONSTRAINTS;
DROP TABLE content                  CASCADE CONSTRAINTS;
DROP TABLE ai_health_analysis       CASCADE CONSTRAINTS;
DROP TABLE pet_health_schedules     CASCADE CONSTRAINTS;
DROP TABLE pet_weight               CASCADE CONSTRAINTS;
DROP TABLE pet_vaccin               CASCADE CONSTRAINTS;
DROP TABLE pet_medical_visits       CASCADE CONSTRAINTS;
DROP TABLE consultations            CASCADE CONSTRAINTS;
DROP TABLE pet                      CASCADE CONSTRAINTS;
DROP TABLE shelter_animals          CASCADE CONSTRAINTS;
DROP TABLE shelter                  CASCADE CONSTRAINTS;
DROP TABLE vet                      CASCADE CONSTRAINTS;
DROP TABLE refresh_token            CASCADE CONSTRAINTS;
DROP TABLE trusted_device           CASCADE CONSTRAINTS;
DROP TABLE users                    CASCADE CONSTRAINTS;
DROP TABLE report                   CASCADE CONSTRAINTS;

/*==============================================================
  1.  USERS (회원)
==============================================================*/
CREATE TABLE users (
    user_id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_id            VARCHAR2(50)   NOT NULL,
    user_pwd            VARCHAR2(200)  NOT NULL,
    provider            VARCHAR2(50),
    provider_id         VARCHAR2(100),
    user_name           VARCHAR2(50)   NOT NULL,
    nickname            VARCHAR2(50)   NOT NULL,
    phone               VARCHAR2(20)   NOT NULL,
    age                 NUMBER(3)      NULL,
    gender              CHAR(1)        NOT NULL,
    address             VARCHAR2(255)   NOT NULL,
    user_email          VARCHAR2(300)  NOT NULL,
    role                VARCHAR2(50)   DEFAULT 'user'      NOT NULL,
    created_at          DATE           DEFAULT SYSDATE     NOT NULL,
    status              VARCHAR2(20)   DEFAULT 'active'    NOT NULL,
    rename_filename     VARCHAR2(255),
    original_filename   VARCHAR2(255),
    face_recognition_id VARCHAR2(255),
    --
    CONSTRAINT uq_users_login       UNIQUE (login_id),
    CONSTRAINT uq_users_name        UNIQUE (user_name),
    CONSTRAINT uq_users_nickname    UNIQUE (nickname),
    CONSTRAINT uq_users_face        UNIQUE (face_recognition_id),
    CONSTRAINT ck_users_gender      CHECK  (gender IN ('M','F')),
    CONSTRAINT ck_users_role        CHECK  (role   IN ('user','admin','vet','shelter')),
    CONSTRAINT ck_users_status      CHECK  (status IN ('active','inactive','suspended'))
);

COMMENT ON TABLE   users                        IS 'DuoPet 회원';
COMMENT ON COLUMN users.user_id                IS 'PK, IDENTITY (회원 고유 식별자)';
COMMENT ON COLUMN users.login_id               IS '로그인 ID';
COMMENT ON COLUMN users.user_pwd               IS 'BCRYPT 해시 비밀번호';
COMMENT ON COLUMN users.provider               IS '소셜로그인 제공자 (KAKAO 등)';
COMMENT ON COLUMN users.provider_id            IS '제공자 발급 고유 ID';
COMMENT ON COLUMN users.user_name              IS '회원 실명';
COMMENT ON COLUMN users.nickname               IS '회원 닉네임';
COMMENT ON COLUMN users.phone                  IS '회원 전화번호';
COMMENT ON COLUMN users.age                    IS '회원 나이';
COMMENT ON COLUMN users.gender                 IS '성별 (M: 남성, F: 여성)';
COMMENT ON COLUMN users.address                IS '회원 주소';
COMMENT ON COLUMN users.user_email             IS '회원 이메일';
COMMENT ON COLUMN users.role                   IS '회원 역할 (user: 일반 사용자, admin: 관리자, vet: 전문가, shelter: 보호소)';
COMMENT ON COLUMN users.created_at             IS '회원 가입일';
COMMENT ON COLUMN users.status                 IS '계정 상태 (active: 활성, inactive: 비활성, suspended: 정지)';
COMMENT ON COLUMN users.rename_filename        IS '회원 프로필 이미지의 서버 저장 파일명';
COMMENT ON COLUMN users.original_filename      IS '회원 프로필 이미지의 원본 파일명';
COMMENT ON COLUMN users.face_recognition_id    IS '얼굴 인식 ID (AI 연동 시 사용)';


-- 1. 관리자
INSERT INTO users (
    login_id, user_pwd, provider, provider_id, user_name, nickname, phone,
    gender, address, user_email, role, created_at, status, rename_filename, original_filename, face_recognition_id
) VALUES
('admin01', 'hashed_password_1', 'NONE', NULL, '관리자', 'admin', '010-1111-1111',
 'M', '서울특별시 강남구 테헤란로 212', 'admin@duopet.com', 'admin', TO_DATE('2024-06-01', 'YYYY-MM-DD'),
 'active', NULL, NULL, 'admin_face_001');

-- 2. 수의사
INSERT INTO users (
    login_id, user_pwd, provider, provider_id, user_name, nickname, phone,
    gender, address, user_email, role, created_at, status, rename_filename, original_filename, face_recognition_id
) VALUES
('vet001', 'hashed_password_2', 'NONE', NULL, '김수의', '동물병원테스트', '010-2222-2222',
 'F', '서울특별시 송파구 올림픽로 300', 'vet@duopet.com', 'vet', TO_DATE('2024-06-02', 'YYYY-MM-DD'),
 'active', NULL, NULL, 'vet_face_001');

-- 3. 보호소 담당자
INSERT INTO users (
    login_id, user_pwd, provider, provider_id, user_name, nickname, phone,
    gender, address, user_email, role, created_at, status, rename_filename, original_filename, face_recognition_id
) VALUES
('shelter01', 'hashed_password_3', 'NONE', NULL, '보호소관리자', 'shelterman', '010-3333-3333',
 'M', '서울특별시 마포구 월드컵북로 400', 'shelter@duopet.com', 'shelter', TO_DATE('2024-06-03', 'YYYY-MM-DD'),
 'active', NULL, NULL, 'shelter_face_001');

-- 4. 일반 사용자
INSERT INTO users (
    login_id, user_pwd, provider, provider_id, user_name, nickname, phone,
    gender, address, user_email, role, created_at, status, rename_filename, original_filename, face_recognition_id
) VALUES
('user01', 'hashed_password_4', 'KAKAO', 'kakao_12345', '홍길동', 'hong', '010-4444-4444',
 'M', '경기도 수원시 영통구 광교중앙로 248', 'user01@duopet.com', 'user', TO_DATE('2024-06-04', 'YYYY-MM-DD'),
 'active', NULL, NULL, 'user01_face');


/*==============================================================
  2.  2-차 인증 기기 (TRUSTED_DEVICE)
==============================================================*/
CREATE TABLE trusted_device (
    device_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         NUMBER          NOT NULL,
    device_info     VARCHAR2(255)   NOT NULL,
    ip_address      VARCHAR2(50)    NOT NULL,
    registered_at   DATE DEFAULT SYSDATE NOT NULL,
    last_used_at    DATE,
    CONSTRAINT fk_td_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
);

COMMENT ON TABLE   trusted_device               IS '신뢰 브라우저/기기 (2차 인증용)';
COMMENT ON COLUMN trusted_device.device_id      IS 'PK, IDENTITY (기기 고유 식별자)';
COMMENT ON COLUMN trusted_device.user_id        IS 'FK (USERS 테이블의 user_id 참조), 사용자 고유 식별자';
COMMENT ON COLUMN trusted_device.device_info    IS 'User-Agent 정보';
COMMENT ON COLUMN trusted_device.ip_address     IS '기기 등록 시 IP 주소';
COMMENT ON COLUMN trusted_device.registered_at  IS '기기 등록일';
COMMENT ON COLUMN trusted_device.last_used_at   IS '기기 최종 사용일';

---

/*==============================================================
  3.  REFRESH_TOKEN
==============================================================*/
CREATE TABLE refresh_token (
    token_id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         NUMBER          NOT NULL,
    refresh_token   VARCHAR2(512)   NOT NULL,
    ip_address      VARCHAR2(50),
    device_info     VARCHAR2(255),
    created_at      DATE DEFAULT SYSDATE NOT NULL,
    expires_at      DATE            NOT NULL,
    token_status    VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL,
    --
    CONSTRAINT uq_refresh_token        UNIQUE (refresh_token),
    CONSTRAINT ck_token_status         CHECK  (token_status IN ('ACTIVE','EXPIRED','REVOKED')),
    CONSTRAINT fk_rt_user              FOREIGN KEY (user_id) REFERENCES users(user_id)
);

COMMENT ON TABLE refresh_token IS 'JWT Refresh 토큰 관리';
COMMENT ON COLUMN refresh_token.token_id       IS 'PK, IDENTITY (토큰 고유 식별자)';
COMMENT ON COLUMN refresh_token.user_id        IS 'FK (USERS 테이블의 user_id 참조), 사용자 고유 식별자';
COMMENT ON COLUMN refresh_token.refresh_token  IS '발급된 Refresh 토큰 값';
COMMENT ON COLUMN refresh_token.ip_address     IS '토큰 발급 시 IP 주소';
COMMENT ON COLUMN refresh_token.device_info    IS '토큰 발급 시 기기 정보 (User-Agent)';
COMMENT ON COLUMN refresh_token.created_at     IS '토큰 발급일';
COMMENT ON COLUMN refresh_token.expires_at     IS '토큰 만료일';
COMMENT ON COLUMN refresh_token.token_status   IS '토큰 상태 (ACTIVE: 활성, EXPIRED: 만료, REVOKED: 철회)';

---

/*==============================================================
  4.  전문가(VET) • 보호소(SHELTER)
==============================================================*/
CREATE TABLE vet (
    vet_id              NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             NUMBER          NOT NULL,
    name                VARCHAR2(50)   NOT NULL,
    license_number      VARCHAR2(200)  NOT NULL UNIQUE,
    phone               VARCHAR2(20),
    email               VARCHAR2(255),
    address             VARCHAR2(255),
    website             VARCHAR2(255),
    specialization      VARCHAR2(100),
    rename_filename     VARCHAR2(255),
    original_filename   VARCHAR2(255),
    CONSTRAINT fk_vet_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

COMMENT ON TABLE vet                             IS '반려동물 전문가(수의사) 정보';
COMMENT ON COLUMN vet.vet_id                   IS 'PK, IDENTITY (전문가 고유 식별자)';
COMMENT ON COLUMN vet.user_id                  IS 'FK (USERS 테이블의 user_id 참조), 사용자 고유 식별자';
COMMENT ON COLUMN vet.name                     IS '전문가(수의사) 이름';
COMMENT ON COLUMN vet.license_number           IS '전문가 면허 번호 (수의사 면허 번호 등)';
COMMENT ON COLUMN vet.phone                    IS '전문가 연락처';
COMMENT ON COLUMN vet.email                    IS '전문가 이메일';
COMMENT ON COLUMN vet.address                  IS '전문가 주소 (병원 주소 등)';
COMMENT ON COLUMN vet.website                  IS '전문가 관련 웹사이트 주소';
COMMENT ON COLUMN vet.specialization           IS '전문 분야';
COMMENT ON COLUMN vet.rename_filename          IS '전문가 프로필 이미지의 서버 저장 파일명';
COMMENT ON COLUMN vet.original_filename        IS '전문가 프로필 이미지의 원본 파일명';

INSERT INTO vet (
    user_id, name, license_number, phone, email,
    address, website, specialization, rename_filename, original_filename
) VALUES (
    2, '김수의', 'VET123456', '010-2222-2222', 'vet@duopet.com',
    '서울특별시 마포구 성미산로 199', 'https://happyvet.co.kr', '내과', NULL, NULL
);

CREATE TABLE shelter (
    shelter_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             NUMBER          NOT NULL,
    shelter_name        VARCHAR2(100)  NOT NULL,
    phone               VARCHAR2(20),
    email               VARCHAR2(255),
    address             VARCHAR2(255),
    website             VARCHAR2(255),
    capacity            NUMBER,
    operating_hours     VARCHAR2(100),
    rename_filename     VARCHAR2(255),
    original_filename   VARCHAR2(255),
    CONSTRAINT fk_shelter_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

COMMENT ON TABLE shelter                          IS '보호소 정보';
COMMENT ON COLUMN shelter.shelter_id             IS 'PK, IDENTITY (보호소 고유 식별자)';
COMMENT ON COLUMN shelter.user_id                IS 'FK (USERS 테이블의 user_id 참조), 사용자 고유 식별자';
COMMENT ON COLUMN shelter.shelter_name           IS '보호소 이름';
COMMENT ON COLUMN shelter.phone                  IS '보호소 연락처';
COMMENT ON COLUMN shelter.email                  IS '보호소 이메일';
COMMENT ON COLUMN shelter.address                IS '보호소 주소';
COMMENT ON COLUMN shelter.website                IS '보호소 웹사이트 주소';
COMMENT ON COLUMN shelter.capacity               IS '보호소 수용 가능 동물 수';
COMMENT ON COLUMN shelter.operating_hours        IS '보호소 운영 시간';
COMMENT ON COLUMN shelter.rename_filename        IS '보호소 프로필 이미지의 서버 저장 파일명';
COMMENT ON COLUMN shelter.original_filename      IS '보호소 프로필 이미지의 원본 파일명';

INSERT INTO shelter (
    user_id, shelter_name, phone, email, address,
    website, capacity, operating_hours, rename_filename, original_filename
) VALUES (
    3, '강아지쉼터', '02-123-4567', 'shelter@duopet.com', '서울특별시 마포구 월드컵북로 400',
    'https://www.duopet-shelter.com', 50, '09:00~18:00', NULL, NULL
);

/*==============================================================
  5.  보호소 동물 • 회원 반려동물
==============================================================*/
CREATE TABLE shelter_animals (
    animal_id           NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    shelter_id          NUMBER        NOT NULL,
    name                VARCHAR2(100) NOT NULL,
    animal_type         VARCHAR2(30)  NOT NULL,
    breed               VARCHAR2(50),
    age                 NUMBER,
    gender              CHAR(1)       NOT NULL,
    neutered            CHAR(1) DEFAULT 'N' NOT NULL,
    status              VARCHAR2(20) DEFAULT 'AVAILABLE' NOT NULL,
    intake_date         DATE DEFAULT SYSDATE NOT NULL,
    description         CLOB,
    profile_image       VARCHAR2(255),
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    updated_at          DATE,
    rename_filename     VARCHAR2(255),
    original_filename   VARCHAR2(255),
    CONSTRAINT ck_sa_gender    CHECK (gender IN ('M','F')),
    CONSTRAINT ck_sa_neutered CHECK (neutered IN ('Y','N')),
    CONSTRAINT ck_sa_status    CHECK (status IN ('AVAILABLE','PENDING_ADOPTION','NOT_AVAILABLE')),
    CONSTRAINT fk_sa_shelter   FOREIGN KEY (shelter_id) REFERENCES shelter(shelter_id)
);

COMMENT ON TABLE shelter_animals                   IS '보호소 등록 동물 정보';
COMMENT ON COLUMN shelter_animals.animal_id       IS 'PK, IDENTITY (동물 고유 식별자)';
COMMENT ON COLUMN shelter_animals.shelter_id      IS 'FK (SHELTER 테이블의 shelter_id 참조), 보호소 고유 식별자';
COMMENT ON COLUMN shelter_animals.name            IS '동물 이름';
COMMENT ON COLUMN shelter_animals.animal_type     IS '동물 종류 (예: 개, 고양이)';
COMMENT ON COLUMN shelter_animals.breed           IS '품종';
COMMENT ON COLUMN shelter_animals.age             IS '나이 (단위: 년)';
COMMENT ON COLUMN shelter_animals.gender          IS '성별 (M: 수컷, F: 암컷)';
COMMENT ON COLUMN shelter_animals.neutered        IS '중성화 여부 (Y: 예, N: 아니오)';
COMMENT ON COLUMN shelter_animals.status          IS '입양 상태 (AVAILABLE: 입양 가능, PENDING_ADOPTION: 입양 대기 중, NOT_AVAILABLE: 입양 불가)';
COMMENT ON COLUMN shelter_animals.intake_date     IS '보호소 입소일';
COMMENT ON COLUMN shelter_animals.description     IS '동물 특징 및 설명';
COMMENT ON COLUMN shelter_animals.profile_image   IS '동물 프로필 이미지 경로';
COMMENT ON COLUMN shelter_animals.created_at      IS '정보 생성일';
COMMENT ON COLUMN shelter_animals.updated_at      IS '정보 최종 수정일';
COMMENT ON COLUMN shelter_animals.rename_filename IS '동물 프로필 이미지의 서버 저장 파일명';
COMMENT ON COLUMN shelter_animals.original_filename IS '동물 프로필 이미지의 원본 파일명';



CREATE TABLE pet (
    pet_id              NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             NUMBER          NOT NULL,
    pet_name            VARCHAR2(50)   NOT NULL,
    animal_type         VARCHAR2(30)   NOT NULL,
    breed               VARCHAR2(50),
    age                 NUMBER,
    gender              CHAR(1)        NOT NULL,
    neutered            CHAR(1) DEFAULT 'N' NOT NULL,
    weight              NUMBER(5,2),
    registration_date   DATE DEFAULT SYSDATE NOT NULL,
    rename_filename     VARCHAR2(255),
    original_filename   VARCHAR2(255),
    CONSTRAINT ck_pet_gender     CHECK (gender IN ('M','F')),
    CONSTRAINT ck_pet_neutered   CHECK (neutered IN ('Y','N')),
    CONSTRAINT fk_pet_user       FOREIGN KEY (user_id) REFERENCES users(user_id)
);

COMMENT ON TABLE pet                            IS '회원 등록 반려동물 정보';
COMMENT ON COLUMN pet.pet_id                    IS 'PK, IDENTITY (반려동물 고유 식별자)';
COMMENT ON COLUMN pet.user_id                   IS 'FK (USERS 테이블의 user_id 참조), 사용자 고유 식별자';
COMMENT ON COLUMN pet.pet_name                  IS '반려동물 이름';
COMMENT ON COLUMN pet.animal_type               IS '반려동물 종류 (예: 개, 고양이)';
COMMENT ON COLUMN pet.breed                     IS '품종';
COMMENT ON COLUMN pet.age                       IS '나이 (단위: 년)';
COMMENT ON COLUMN pet.gender                    IS '성별 (M: 수컷, F: 암컷)';
COMMENT ON COLUMN pet.neutered                  IS '중성화 여부 (Y: 예, N: 아니오)';
COMMENT ON COLUMN pet.weight                    IS '체중 (단위: kg)';
COMMENT ON COLUMN pet.registration_date         IS '반려동물 등록일';
COMMENT ON COLUMN pet.rename_filename           IS '반려동물 프로필 이미지의 서버 저장 파일명';
COMMENT ON COLUMN pet.original_filename         IS '반려동물 프로필 이미지의 원본 파일명';


-- 초코
INSERT INTO pet (
    user_id, pet_name, animal_type, breed, age,
    gender, neutered, weight, registration_date,
    rename_filename, original_filename
) VALUES 
(4, '초코', '강아지', '말티즈', 4, 'M', 'Y', 3.2,
 TO_DATE('2022-03-20', 'YYYY-MM-DD'), NULL, NULL);

-- 루나
INSERT INTO pet (
    user_id, pet_name, animal_type, breed, age,
    gender, neutered, weight, registration_date,
    rename_filename, original_filename
) VALUES 
(4, '루나', '고양이', '러시안블루', 1, 'F', 'N', 2.8,
 TO_DATE('2024-01-10', 'YYYY-MM-DD'), NULL, NULL);

/*==============================================================
  6.  상담·진료·건강관리 (주요 FK: pet / vet / user)
==============================================================*/
CREATE TABLE consultations (
    consultation_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         NUMBER        NOT NULL,
    vet_id          NUMBER        NOT NULL,
    category        VARCHAR2(50)  NOT NULL,
    priority        VARCHAR2(50)  DEFAULT '보통' NOT NULL,
    title           VARCHAR2(255) NOT NULL,
    description     CLOB          NOT NULL,
    status          VARCHAR2(20)  DEFAULT '접수' NOT NULL,
    created_at      DATE DEFAULT SYSDATE NOT NULL,
    updated_at      DATE,
    answer          CLOB,
    CONSTRAINT ck_consult_priority CHECK (priority IN ('보통','긴급')),
    CONSTRAINT ck_consult_status   CHECK (status   IN ('접수','진행중','완료')),
    CONSTRAINT fk_consult_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_consult_vet  FOREIGN KEY (vet_id)  REFERENCES vet(vet_id)
);

COMMENT ON TABLE consultations                  IS '전문가 상담 내역';
COMMENT ON COLUMN consultations.consultation_id IS 'PK, IDENTITY (상담 고유 식별자)';
COMMENT ON COLUMN consultations.user_id         IS 'FK (USERS 테이블의 user_id 참조), 상담 신청 회원 고유 식별자';
COMMENT ON COLUMN consultations.vet_id          IS 'FK (VET 테이블의 vet_id 참조), 상담 담당 전문가 고유 식별자';
COMMENT ON COLUMN consultations.category        IS '상담 카테고리 (예: 건강, 행동, 영양)';
COMMENT ON COLUMN consultations.priority        IS '상담 우선순위 (보통, 긴급)';
COMMENT ON COLUMN consultations.title           IS '상담 제목';
COMMENT ON COLUMN consultations.description     IS '상담 내용 (사용자가 작성한 질문/설명)';
COMMENT ON COLUMN consultations.status          IS '상담 상태 (접수, 진행중, 완료)';
COMMENT ON COLUMN consultations.created_at      IS '상담 접수일';
COMMENT ON COLUMN consultations.updated_at      IS '상담 최종 수정일';
COMMENT ON COLUMN consultations.answer          IS '전문가의 상담 답변 내용';

INSERT INTO consultations (
    user_id, vet_id, category, priority,
    title, description, status, created_at, updated_at, answer
) VALUES
(4, 1, '건강', '보통',
 '급성 기침 상담',
 '강아지 초코가 최근 밤마다 기침을 합니다. 원인과 처치법이 궁금합니다.',
 '완료', SYSDATE, NULL,
 '기관지염 초기일 수 있습니다. 실내 습도를 50-60%로 유지하시고 3일 이상 지속되면 내원하세요.');

INSERT INTO consultations (
    user_id, vet_id, category, priority,
    title, description, status, created_at, updated_at, answer
) VALUES
(4, 1, '영양', '보통',
 '사료 교체 주기 문의',
 '4살 말티즈 “초코”의 사료를 언제, 어떻게 바꾸면 좋을까요?',
 '완료', SYSDATE, NULL,
 '4-6개월 간격으로 단계적으로 혼합 급여하며 장 관찰 후 교체하세요.');

INSERT INTO consultations (
    user_id, vet_id, category, priority,
    title, description, status, created_at, updated_at, answer
) VALUES
(4, 1, '행동', '보통',
 '심야 짖음 문제',
 '고양이 “루나”가 새벽에 계속 울어서 고민입니다.',
 '완료', SYSDATE, NULL,
 '환경 풍부화(캣타워, 장난감)와 주간 활동량 증가로 해결 가능성이 높습니다.');



CREATE TABLE pet_medical_visits (
    visit_id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pet_id          NUMBER        NOT NULL,
    hospital_name   VARCHAR2(100) NOT NULL,
    veterinarian    VARCHAR2(100) NOT NULL,
    visit_date      DATE          NOT NULL,
    visit_reason    VARCHAR2(255) NOT NULL,
    diagnosis       VARCHAR2(1000),
    treatment       VARCHAR2(1000),
    cost            NUMBER(10,2),
    created_at      DATE DEFAULT SYSDATE NOT NULL,
    updated_at      DATE,
    CONSTRAINT fk_visit_pet FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);

COMMENT ON TABLE pet_medical_visits                  IS '반려동물 진료 기록';
COMMENT ON COLUMN pet_medical_visits.visit_id        IS 'PK, IDENTITY (진료 기록 고유 식별자)';
COMMENT ON COLUMN pet_medical_visits.pet_id          IS 'FK (PET 테이블의 pet_id 참조), 반려동물 고유 식별자';
COMMENT ON COLUMN pet_medical_visits.hospital_name   IS '방문 병원 이름';
COMMENT ON COLUMN pet_medical_visits.veterinarian    IS '진료 수의사 이름';
COMMENT ON COLUMN pet_medical_visits.visit_date      IS '진료 방문일';
COMMENT ON COLUMN pet_medical_visits.visit_reason    IS '방문 사유';
COMMENT ON COLUMN pet_medical_visits.diagnosis       IS '진단명';
COMMENT ON COLUMN pet_medical_visits.treatment       IS '치료 내용';
COMMENT ON COLUMN pet_medical_visits.cost            IS '진료 비용';
COMMENT ON COLUMN pet_medical_visits.created_at      IS '기록 생성일';
COMMENT ON COLUMN pet_medical_visits.updated_at      IS '기록 최종 수정일';

INSERT INTO pet_medical_visits (
    pet_id, hospital_name, veterinarian, visit_date,
    visit_reason, diagnosis, treatment, cost, created_at, updated_at
) VALUES
(1, '행복동물병원', '김수의', TO_DATE('2025-06-05', 'YYYY-MM-DD'),
 '정기 건강검진', '특이소견 없음', '기초 혈액검사·구충제 처방', 55000,
 SYSDATE, NULL);

INSERT INTO pet_medical_visits (
    pet_id, hospital_name, veterinarian, visit_date,
    visit_reason, diagnosis, treatment, cost, created_at, updated_at
) VALUES
(2, '행복동물병원', '김수의', TO_DATE('2025-06-10', 'YYYY-MM-DD'),
 '귀 소양감', '외이염 초기', '외이 세정·항생제 연고', 43000,
 SYSDATE, NULL);

CREATE TABLE pet_vaccin (
    vaccination_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pet_id              NUMBER        NOT NULL,
    vaccine_name        VARCHAR2(100) NOT NULL,
    scheduled_date      DATE          NOT NULL,
    description         VARCHAR2(1000),
    administered_date   DATE,
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    updated_at          DATE,
    CONSTRAINT fk_vaccin_pet FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);

COMMENT ON TABLE pet_vaccin                     IS '반려동물 예방 접종 기록';
COMMENT ON COLUMN pet_vaccin.vaccination_id     IS 'PK, IDENTITY (접종 기록 고유 식별자)';
COMMENT ON COLUMN pet_vaccin.pet_id             IS 'FK (PET 테이블의 pet_id 참조), 반려동물 고유 식별자';
COMMENT ON COLUMN pet_vaccin.vaccine_name       IS '백신 이름';
COMMENT ON COLUMN pet_vaccin.scheduled_date     IS '예정된 접종일';
COMMENT ON COLUMN pet_vaccin.description        IS '예방 접종 설명';
COMMENT ON COLUMN pet_vaccin.administered_date  IS '실제 접종일';
COMMENT ON COLUMN pet_vaccin.created_at         IS '기록 생성일';
COMMENT ON COLUMN pet_vaccin.updated_at         IS '기록 최종 수정일';

INSERT INTO pet_vaccin (
    pet_id, vaccine_name, scheduled_date,
    description, administered_date, created_at, updated_at
) VALUES
(1, '종합백신 5-way',
 TO_DATE('2025-06-05', 'YYYY-MM-DD'), '연례 종합 예방접종', NULL,
 SYSDATE, NULL);

INSERT INTO pet_vaccin (
    pet_id, vaccine_name, scheduled_date,
    description, administered_date, created_at, updated_at
) VALUES
(2, '광견병', TO_DATE('2025-06-10', 'YYYY-MM-DD'),
 '생후 12개월 광견병 1차', NULL,
 SYSDATE, NULL);


CREATE TABLE pet_weight (
    weight_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pet_id          NUMBER      NOT NULL,
    weight_kg       NUMBER(5,2) NOT NULL,
    measured_date   DATE        NOT NULL,
    memo            VARCHAR2(500),
    created_at      DATE DEFAULT SYSDATE NOT NULL,
    updated_at      DATE,
    CONSTRAINT fk_weight_pet FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);

COMMENT ON TABLE pet_weight                  IS '반려동물 체중 기록';
COMMENT ON COLUMN pet_weight.weight_id       IS 'PK, IDENTITY (체중 기록 고유 식별자)';
COMMENT ON COLUMN pet_weight.pet_id          IS 'FK (PET 테이블의 pet_id 참조), 반려동물 고유 식별자';
COMMENT ON COLUMN pet_weight.weight_kg       IS '체중 (킬로그램)';
COMMENT ON COLUMN pet_weight.measured_date   IS '측정일';
COMMENT ON COLUMN pet_weight.memo            IS '메모';
COMMENT ON COLUMN pet_weight.created_at      IS '기록 생성일';
COMMENT ON COLUMN pet_weight.updated_at      IS '기록 최종 수정일';

INSERT INTO pet_weight (
    pet_id, weight_kg, measured_date,
    memo, created_at, updated_at
) VALUES
(1, 3.25, TRUNC(SYSDATE), '식사량 정상', SYSDATE, NULL);

INSERT INTO pet_weight (
    pet_id, weight_kg, measured_date,
    memo, created_at, updated_at
) VALUES
(2, 2.80, TRUNC(SYSDATE), '성장기 정상 체중', SYSDATE, NULL);


CREATE TABLE pet_health_schedules (
    schedule_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pet_id          NUMBER        NOT NULL,
    schedule_type   VARCHAR2(50)  NOT NULL,
    title           VARCHAR2(255) NOT NULL,
    schedule_date   DATE          NOT NULL,
    schedule_time   VARCHAR2(10),
    memo            VARCHAR2(500),
    created_at      DATE DEFAULT SYSDATE NOT NULL,
    updated_at      DATE,
    CONSTRAINT fk_phs_pet FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);

COMMENT ON TABLE pet_health_schedules              IS '반려동물 건강 관리 일정';
COMMENT ON COLUMN pet_health_schedules.schedule_id   IS 'PK, IDENTITY (일정 고유 식별자)';
COMMENT ON COLUMN pet_health_schedules.pet_id        IS 'FK (PET 테이블의 pet_id 참조), 반려동물 고유 식별자';
COMMENT ON COLUMN pet_health_schedules.schedule_type IS '일정 종류 (예: 접종, 미용, 산책, 병원 방문)';
COMMENT ON COLUMN pet_health_schedules.title         IS '일정 제목';
COMMENT ON COLUMN pet_health_schedules.schedule_date IS '일정 날짜';
COMMENT ON COLUMN pet_health_schedules.schedule_time IS '일정 시간';
COMMENT ON COLUMN pet_health_schedules.memo          IS '일정 관련 메모';
COMMENT ON COLUMN pet_health_schedules.created_at    IS '기록 생성일';
COMMENT ON COLUMN pet_health_schedules.updated_at    IS '기록 최종 수정일';

INSERT INTO pet_health_schedules (
    pet_id, schedule_type, title,
    schedule_date, schedule_time, memo,
    created_at, updated_at
) VALUES
(1, '백신', '종합백신 예약',
 TO_DATE('2025-08-04', 'YYYY-MM-DD'), '10:00', '병원 방문 예약 완료',
 SYSDATE, NULL);

INSERT INTO pet_health_schedules (
    pet_id, schedule_type, title,
    schedule_date, schedule_time, memo,
    created_at, updated_at
) VALUES
(1, '체중', '월간 체중 측정',
 TO_DATE('2025-07-15', 'YYYY-MM-DD'), '19:00', '집에서 측정',
 SYSDATE, NULL);


CREATE TABLE ai_health_analysis (
    analysis_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pet_id              NUMBER        NOT NULL,
    analysis_type       VARCHAR2(30)  NOT NULL,
    media_path          VARCHAR2(500),
    symptom_desc        CLOB,
    ai_result_summary   VARCHAR2(300) NOT NULL,
    ai_result_detail    CLOB,
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT ck_ai_type CHECK (analysis_type IN ('image','video','realtime')),
    CONSTRAINT fk_ai_pet  FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);

COMMENT ON TABLE ai_health_analysis                  IS 'AI 건강 분석 기록';
COMMENT ON COLUMN ai_health_analysis.analysis_id       IS 'PK, IDENTITY (분석 기록 고유 식별자)';
COMMENT ON COLUMN ai_health_analysis.pet_id            IS 'FK (PET 테이블의 pet_id 참조), 반려동물 고유 식별자';
COMMENT ON COLUMN ai_health_analysis.analysis_type     IS '분석 타입 (image: 이미지, video: 영상, realtime: 실시간)';
COMMENT ON COLUMN ai_health_analysis.media_path        IS '분석에 사용된 미디어 파일 경로';
COMMENT ON COLUMN ai_health_analysis.symptom_desc      IS '사용자가 입력한 증상 설명';
COMMENT ON COLUMN ai_health_analysis.ai_result_summary IS 'AI 분석 결과 요약';
COMMENT ON COLUMN ai_health_analysis.ai_result_detail  IS 'AI 분석 결과 상세 내용';
COMMENT ON COLUMN ai_health_analysis.created_at        IS '분석 기록 생성일';


/*==============================================================
  7.  게시글/댓글/FAQ/좋아요/북마크
==============================================================*/
CREATE TABLE content (
    content_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             NUMBER           NOT NULL,
    title               VARCHAR2(1000)   NOT NULL,
    content_body        CLOB             NOT NULL,
    content_type        VARCHAR2(50)     DEFAULT 'board' NOT NULL,
    category            VARCHAR2(200)    DEFAULT 'free',
    tags                VARCHAR2(100),
    view_count          NUMBER DEFAULT 0 NOT NULL,
    like_count          NUMBER DEFAULT 0,
    rename_filename     VARCHAR2(255),
    original_filename   VARCHAR2(255),
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    update_at           DATE,
    CONSTRAINT ck_content_type CHECK (content_type IN ('notice','qna','info_board','board')),
    CONSTRAINT ck_content_category  CHECK (category IN ('정보광장','질병정보','음식정보','자유','후기','질문', '팁')),
    CONSTRAINT fk_content_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

COMMENT ON TABLE content                        IS '게시글 정보 (공지, qna, 정보게시판, 자유게시판 포함)';
COMMENT ON COLUMN content.content_id          IS 'PK, IDENTITY (게시글 고유 식별자)';
COMMENT ON COLUMN content.user_id             IS 'FK (USERS 테이블의 user_id 참조), 작성자 고유 식별자';
COMMENT ON COLUMN content.title               IS '게시글 제목';
COMMENT ON COLUMN content.content_body        IS '게시글 본문 내용';
COMMENT ON COLUMN content.content_type        IS '게시글 유형 (notice: 공지사항, qna: qna, info_board: 정보 게시판, board: 자유 게시판)';
COMMENT ON COLUMN content.category            IS '게시글 카테고리 (정보광장, 질병정보, 음식정보, 자유, 후기, 질문, 팁)';
COMMENT ON COLUMN content.tags                IS '게시글 관련 태그 (콤마로 구분)';
COMMENT ON COLUMN content.view_count          IS '조회수';
COMMENT ON COLUMN content.like_count          IS '좋아요 수';
COMMENT ON COLUMN content.rename_filename     IS '첨부 파일의 서버 저장 파일명';
COMMENT ON COLUMN content.original_filename   IS '첨부 파일의 원본 파일명';
COMMENT ON COLUMN content.created_at          IS '게시글 작성일';
COMMENT ON COLUMN content.update_at           IS '게시글 최종 수정일';


INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    1,
    '시스템 점검 안내',
    '안녕하세요, 회원님들께 더 나은 서비스를 제공하기 위해 2025년 6월 20일(금) 오전 2시부터 4시까지 시스템 점검이 진행됩니다. 점검 시간 동안 서비스 이용이 일시 중단되오니 양해 부탁드립니다.',
    'notice',
    null,
    null,
    0,
    0,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    1,
    '여름 휴가철 고객센터 운영 안내',
    '다가오는 여름 휴가철을 맞아 고객센터 운영 시간이 변경됩니다. 7월 15일부터 8월 15일까지 평일 오전 9시부터 오후 6시까지 운영되며, 주말 및 공휴일은 휴무입니다.',
    'notice',
    null,
    null,
    0,
    0,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    1,
    '시스템 점검 안내',
    '안녕하세요, 회원님들께 더 나은 서비스를 제공하기 위해 2025년 6월 20일(금) 오전 2시부터 4시까지 시스템 점검이 진행됩니다. 점검 시간 동안 서비스 이용이 일시 중단되오니 양해 부탁드립니다.',
    'notice',
    null,
    null,
    0,
    0,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    1, -- 예시 사용자 ID
    '운영자님, 사이트 오류 보고합니다.',
    '안녕하세요 운영자님. 오늘 오전 10시경, 게시글 수정 기능을 사용하려는데 "페이지를 찾을 수 없습니다" 오류가 발생했습니다. 확인 부탁드립니다.',
    'qna',
    NULL,
    NULL,
    0,
    0,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    1, -- 예시 사용자 ID (첫 번째와 다르게 설정)
    '운영자님께 문의드립니다 - 계정 관련',
    '운영자님, 제 계정 정보(이메일 주소)를 변경하고 싶은데, 마이페이지에서 해당 메뉴를 찾을 수가 없습니다. 어떻게 변경해야 하나요?',
    'qna',
    NULL,
    NULL,
    0,
    0,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

-- 자유 게시판 
INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    3, -- 예시 사용자 ID
    '우리 고양이가 새 장난감을 너무 좋아해요',
    '새로 장만한 장난감을 우리 냥이가 너무 좋아해서 공유합니다!',
    'board',
    '자유',
    '고양이,장난감,놀이',
    110,
    46,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '반려동물과 함께한 첫 캠핑 후기',
    '텐트 안에서도 얌전하게 있었어요. 꿀팁도 나눌게요!',
    'board',
    '자유',
    '캠핑,야외활동,고양이',
    278,
    21,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    3, -- 예시 사용자 ID
    '강아지 셀프 목욕했어요!',
    '욕조에서 가만히 있네요 ㅎㅎ 셀프목욕 성공!',
    'board',
    '자유',
    '강아지,목욕,셀프관리',
    298,
    45,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    4, -- 예시 사용자 ID
    '고양이 분양 받았어요',
    '고양이 처음 키우는데 너무 설레고 기뻐요~',
    'board',
    '자유',
    '입양,고양이,첫날',
    206,
    68,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    3, -- 예시 사용자 ID
    '강아지 샴푸 추천 후기',
    '향도 좋고 자극도 없어요. 사용기 남깁니다.',
    'board',
    '후기',
    '샴푸,강아지용품',
    228,
    69,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '고양이 캣타워 후기',
    '튼튼하고 설치도 쉬워요! 고양이도 좋아하네요.',
    'board',
    '후기',
    '캣타워,후기,가구',
    228,
    17,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    3, -- 예시 사용자 ID
    '영양제 급여 후기',
    '먹인 지 1주일인데 활력이 돌아왔어요.',
    'board',
    '후기',
    '영양제,강아지건강',
    223,
    52,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '간식 후기 - 닭가슴살 스틱',
    '알러지 없고 기호성도 좋아요!',
    'board',
    '후기',
    '간식,기호성,고양이',
    293,
    19,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


-- 팁 게시판
INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '고양이 털 날림 줄이는 팁',
    '빗질은 아침마다! 많이 줄어요.',
    'board',
    '팁',
    '고양이,털관리,브러쉬',
    242,
    66,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '강아지 산책 시간대 조절',
    '낮보다는 해지기 전이 덜 더워서 좋아요.',
    'board',
    '팁',
    '산책,계절,팁',
    208,
    33,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '물그릇 대신 분수기 추천',
    '물 마시는 양이 확 늘었어요!',
    'board',
    '팁',
    '음수량,건강팁',
    150,
    52,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '고양이 화장실 모래 선택기',
    '고운 모래가 발에도 안 붙고 좋아요.',
    'board',
    '팁',
    '고양이,모래,팁',
    256,
    28,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


-- 질문 게시판
INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    4, -- 예시 사용자 ID
    '강아지 자꾸 발 핥는데 왜 그럴까요?',
    '염증인지 습관인지 헷갈려요.',
    'board',
    '질문',
    '강아지,발핥기,질문',
    141,
    11,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '고양이 귀 안이 붉어요',
    '염증일까요? 병원 가야할지 고민돼요.',
    'board',
    '질문',
    '고양이,귀,건강',
    202,
    11,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '슬개골 탈구 수술 해야 할까요?',
    '경미하다고는 하는데 미뤄도 될까요?',
    'board',
    '질문',
    '슬개골,강아지,정형',
    290,
    46,
    NULL,
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO content (
    user_id,
    title,
    content_body,
    content_type,
    category,
    tags,
    view_count,
    like_count,
    rename_filename,
    original_filename,
    created_at,
    update_at
) VALUES (
    2, -- 예시 사용자 ID
    '사료 변경 주기?',
    '몇 개월마다 바꿔줘야 하는지 궁금해요.',
    'board',
    '질문',
    '사료,영양,변경',
    123,
    31,
    NULL,
    NULL,
    SYSDATE,
    NULL
);


CREATE TABLE comments (
    comment_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content_id          NUMBER        NOT NULL,
    user_id             NUMBER        NOT NULL,
    content             VARCHAR2(1000) NOT NULL,
    parent_comment_id   NUMBER,
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    update_at           DATE,
    CONSTRAINT fk_comment_content  FOREIGN KEY (content_id)          REFERENCES content(content_id),
    CONSTRAINT fk_comment_user     FOREIGN KEY (user_id)             REFERENCES users(user_id),
    CONSTRAINT fk_comment_parent   FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id)
);

COMMENT ON TABLE comments                           IS '댓글 정보';
COMMENT ON COLUMN comments.comment_id             IS 'PK, IDENTITY (댓글 고유 식별자)';
COMMENT ON COLUMN comments.content_id             IS 'FK (CONTENT 테이블의 content_id 참조), 댓글이 속한 게시글 고유 식별자';
COMMENT ON COLUMN comments.user_id                IS 'FK (USERS 테이블의 user_id 참조), 댓글 작성자 고유 식별자';
COMMENT ON COLUMN comments.content                IS '댓글 내용';
COMMENT ON COLUMN comments.parent_comment_id      IS 'FK (댓글-대댓글 관계 시 상위 댓글의 comment_id 참조), 부모 댓글 고유 식별자';
COMMENT ON COLUMN comments.created_at             IS '댓글 작성일';
COMMENT ON COLUMN comments.update_at              IS '댓글 최종 수정일';


INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    4, -- 답변하려는 QnA 게시글의 content_id (예시 값, 실제 값으로 변경 필요)
    1,   -- 답변하는 사용자의 user_id (예: 운영자 계정 ID)
    '안녕하세요. 문의주신 모바일 페이지 로딩 속도 문제는 현재 인지하고 있으며, 개선 작업을 진행 중에 있습니다. 최대한 빠르게 해결하여 불편함 없이 이용하실 수 있도록 노력하겠습니다. 감사합니다.',
    NULL, -- QnA 게시글에 대한 직접적인 답변이므로 NULL
    SYSDATE,
    NULL
);

-- 자유 게시판 
INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    5,
    2,
    '저희 고양이도 장난감 물고 다녀요. 귀엽죠!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    5,
    4,
    '어떤 장난감인지 궁금하네요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    6,
    3,
    '캠핑 후기 잘 봤어요! 다음엔 저도 도전!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    6,
    4,
    '야외에서 반려동물과 함께라니 멋져요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    7,
    2,
    '셀프 목욕 성공 축하드려요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    7,
    4,
    '욕조에 얌전히 있다니 대단하네요!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    8,
    2,
    '입양 축하드려요! 첫날은 항상 설레죠.',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    8,
    3,
    '고양이 첫날 이야기 공감돼요!',
    NULL,
    SYSDATE,
    NULL
);

-- 후기 게시판 
INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    9,
    2,
    '이 샴푸 저도 써봤는데 정말 괜찮더라구요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    9,
    4,
    '향이 좋다니 써보고 싶어요!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    10,
    3,
    '저희 집 캣타워도 설치 쉽고 좋아요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    10,
    4,
    '설치 후기 감사합니다!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    11,
    2,
    '영양제 급여 꿀팁 감사해요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    11,
    4,
    '활력이 돌아왔다니 다행이네요!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    12,
    3,
    '닭가슴살 스틱 정말 인기 많죠~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    12,
    4,
    '기호성 중요하죠. 후기 감사합니다!',
    NULL,
    SYSDATE,
    NULL
);

-- 팁 게시판 
INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    13,
    3,
    '아침 빗질 팁 유용하네요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    13,
    4,
    '털관리 어렵던데 좋은 정보에요!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    14,
    3,
    '맞아요 저녁 산책이 덜 덥더라구요',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    14,
    4,
    '시간대 정보 도움됐어요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    15,
    3,
    '분수기 쓰니까 애가 물 많이 마셔요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    15,
    4,
    '음수량 정말 중요하죠. 추천 감사합니다!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    16,
    3,
    '고운 모래 저도 사용 중인데 좋아요',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    16,
    4,
    '발에 안 붙는 모래는 필수죠~',
    NULL,
    SYSDATE,
    NULL
);

-- 질문 게시판

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    17,
    2,
    '저희 아이도 발 자주 핥아요. 병원 상담 추천드려요!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    17,
    3,
    '염증 가능성 있으니 진찰 받아보세요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    18,
    3,
    '귀 안 붉으면 진드기일 수도 있어요',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    18,
    4,
    '병원 상담 한번 받아보심이~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    19,
    3,
    '경미해도 상황 봐가며 결정하세요!',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    19,
    4,
    '수술 여부는 병원에서 자세히 상담 받으세요~',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    20,
    3,
    '사료는 보통 3~6개월 주기로 바꿔요',
    NULL,
    SYSDATE,
    NULL
);

INSERT INTO comments (
    content_id,
    user_id,
    content,
    parent_comment_id,
    created_at,
    update_at
) VALUES (
    20,
    4,
    '너무 자주 바꾸면 탈 생길 수도 있어요~',
    NULL,
    SYSDATE,
    NULL
);


CREATE TABLE faq (
    faq_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id   NUMBER          NOT NULL,
    question  VARCHAR2(1000) NOT NULL,
    answer    VARCHAR2(4000) NOT NULL,
    CONSTRAINT fk_faq_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

COMMENT ON TABLE faq                  IS '자주 묻는 질문(FAQ)';
COMMENT ON COLUMN faq.faq_id        IS 'PK, IDENTITY (FAQ 고유 식별자)';
COMMENT ON COLUMN faq.user_id       IS 'FK (USERS 테이블의 user_id 참조), FAQ 작성자/관리자 고유 식별자';
COMMENT ON COLUMN faq.question      IS '질문 내용';
COMMENT ON COLUMN faq.answer        IS '답변 내용';

INSERT INTO faq (
    user_id, question, answer
) VALUES
(1,
 '사료는 얼마나 자주 교체해야 하나요?',
 '일반적으로 4-6개월 간격으로 단계적으로 교체하며, 장 트러블 여부를 관찰하세요.');


CREATE TABLE LIKE_TB (
    like_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER        NOT NULL,
    target_id   NUMBER        NOT NULL,
    target_type VARCHAR2(50)  NOT NULL,
    created_at  DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT ck_like_type CHECK (target_type IN ('board','comment','info_board')),
    CONSTRAINT uq_like_once UNIQUE (user_id, target_id, target_type),
    CONSTRAINT fk_like_user  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

COMMENT ON TABLE LIKE_TB                     IS '좋아요 기록';
COMMENT ON COLUMN LIKE_TB.like_id          IS 'PK, IDENTITY (좋아요 고유 식별자)';
COMMENT ON COLUMN LIKE_TB.user_id          IS 'FK (USERS 테이블의 user_id 참조), 좋아요를 누른 사용자 고유 식별자';
COMMENT ON COLUMN LIKE_TB.target_id        IS '좋아요 대상의 ID (게시글 ID, 댓글 ID 등)';
COMMENT ON COLUMN LIKE_TB.target_type      IS '좋아요 대상의 유형 (board: 게시글, comment: 댓글, info_board: 정보 게시판)';
COMMENT ON COLUMN LIKE_TB.created_at       IS '좋아요 누른 시간';

INSERT INTO LIKE_TB (
    user_id, target_id, target_type, created_at
) VALUES
(4, 20, 'board', SYSDATE);

INSERT INTO LIKE_TB (
    user_id, target_id, target_type, created_at
) VALUES
(2, 14, 'board',   SYSDATE);

INSERT INTO LIKE_TB (
    user_id, target_id, target_type, created_at
) VALUES
(3, 15, 'board',   SYSDATE);

INSERT INTO LIKE_TB (
    user_id, target_id, target_type, created_at
) VALUES
(2,  5, 'comment', SYSDATE);



CREATE TABLE bookmark (
    bookmark_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER        NOT NULL,
    content_id  NUMBER        NOT NULL,
    target_type VARCHAR2(50)  DEFAULT '자유' NOT NULL,
    created_at  DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT ck_bm_type CHECK (target_type IN ('정보광장','질병정보','음식정보','자유','후기','질문', '팁')),
    CONSTRAINT fk_bm_user    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT fk_bm_content FOREIGN KEY (content_id) REFERENCES content(content_id),
    CONSTRAINT uq_bm_once    UNIQUE (user_id, content_id)
);

COMMENT ON TABLE bookmark                      IS '북마크 기록';
COMMENT ON COLUMN bookmark.bookmark_id       IS 'PK, IDENTITY (북마크 고유 식별자)';
COMMENT ON COLUMN bookmark.user_id           IS 'FK (USERS 테이블의 user_id 참조), 북마크를 한 사용자 고유 식별자';
COMMENT ON COLUMN bookmark.content_id        IS 'FK (CONTENT 테이블의 content_id 참조), 북마크 대상 게시글 고유 식별자';
COMMENT ON COLUMN bookmark.target_type       IS '북마크 대상의 유형 (정보광장, 질병정보, 음식정보, 자유, 후기, 질문, 팁)';
COMMENT ON COLUMN bookmark.created_at        IS '북마크 생성일';

INSERT INTO bookmark (
    user_id, content_id, target_type, created_at
) VALUES
(4, 20, '질문', SYSDATE);

INSERT INTO bookmark (
    user_id, content_id, target_type, created_at
) VALUES
(2, 14, '팁', SYSDATE);

INSERT INTO bookmark (
    user_id, content_id, target_type, created_at
) VALUES
(3, 15, '팁', SYSDATE);

INSERT INTO bookmark (
    user_id, content_id, target_type, created_at
) VALUES
(4, 17, '팁', SYSDATE);

/*==============================================================
  9. 완료
==============================================================*/
commit;



/*================
2025-07-09 수정
테스트계정 비밀번호 암호화
=================*/

UPDATE users
SET user_pwd = '$2a$10$q1HEMWX8fpM0bWTqKWioROp4Sk/xAdD.46PQKHNfNEP9r0C6cmMqe'
WHERE login_id = 'user01';

UPDATE users
SET user_pwd = '$2a$10$q1HEMWX8fpM0bWTqKWioROp4Sk/xAdD.46PQKHNfNEP9r0C6cmMqe'
WHERE login_id = 'admin01';

UPDATE users
SET user_pwd = '$2a$10$q1HEMWX8fpM0bWTqKWioROp4Sk/xAdD.46PQKHNfNEP9r0C6cmMqe'
WHERE login_id = 'vet001';

UPDATE users
SET user_pwd = '$2a$10$q1HEMWX8fpM0bWTqKWioROp4Sk/xAdD.46PQKHNfNEP9r0C6cmMqe'
WHERE login_id = 'shelter01';

commit;



/*================
2025-07-10 수정
USERS 테이블 STATUS 제약조건 수정
=================*/

-- 1. 기존 제약 조건 삭제
ALTER TABLE users DROP CONSTRAINT CK_USERS_STATUS;

-- 2. 새로운 상태값 포함해서 다시 추가
ALTER TABLE users
ADD CONSTRAINT CK_USERS_STATUS
CHECK (status IN ('active', 'inactive', 'suspended', 'waiting', 'rejected', 'social_temp'));

COMMENT ON COLUMN users.status IS '계정 상태 (active: 활성, inactive: 비활성, suspended: 정지, waiting: 전문가/보호소 승인 대기,rejected: 전문가/보호소 승인 거절, social_temp:첫소셜가입자)';

commit;

/*================
2025-07-11 수정
SHELTER_ANIMALS 테이블에 공공 API 연동을 위한 컬럼 추가
=================*/
-- SHELTER_ANIMALS 테이블에 공공 API 연동을 위한 컬럼 추가
ALTER TABLE SHELTER_ANIMALS ADD (
    desertion_no VARCHAR2(50),              -- 유기번호
    happen_date DATE,                       -- 발견일
    happen_place VARCHAR2(255),             -- 발견장소
    special_mark VARCHAR2(1000),            -- 특징
    public_notice_no VARCHAR2(50),          -- 공고번호
    public_notice_start DATE,               -- 공고시작일
    public_notice_end DATE,                 -- 공고종료일
    image_url VARCHAR2(500),                -- 공공 API 이미지 URL
    api_source VARCHAR2(50),                -- API 출처 구분
    weight NUMBER(5,2),                     -- 체중
    color_cd VARCHAR2(50),                  -- 색상
    process_state VARCHAR2(20)              -- 상태 (protect, return, adopt 등)
);

-- 유기번호에 대한 유니크 인덱스 생성
CREATE UNIQUE INDEX idx_desertion_no ON SHELTER_ANIMALS(desertion_no);

-- 보호소 테이블에 공공 API 연동을 위한 컬럼 추가
ALTER TABLE SHELTER ADD (
    care_reg_no VARCHAR2(50),               -- 공공 API 보호소 등록번호
    api_sync_enabled CHAR(1) DEFAULT 'N',   -- API 동기화 여부
    last_sync_date DATE                     -- 마지막 동기화 시간
);

-- 보호소 등록번호 인덱스
CREATE UNIQUE INDEX idx_care_reg_no ON SHELTER(care_reg_no);

-- 시퀀스 생성 (없는 경우)
CREATE SEQUENCE SEQ_SHELTER_ANIMALS START WITH 1 INCREMENT BY 1;

-- 보호소 이름으로 조회할 수 있도록 인덱스 추가
CREATE INDEX idx_shelter_name ON SHELTER(shelter_name);

-- 주석 추가
COMMENT ON COLUMN SHELTER_ANIMALS.desertion_no IS '공공 API 유기번호';
COMMENT ON COLUMN SHELTER_ANIMALS.happen_date IS '발견일';
COMMENT ON COLUMN SHELTER_ANIMALS.happen_place IS '발견장소';
COMMENT ON COLUMN SHELTER_ANIMALS.special_mark IS '특징';
COMMENT ON COLUMN SHELTER_ANIMALS.public_notice_no IS '공고번호';
COMMENT ON COLUMN SHELTER_ANIMALS.public_notice_start IS '공고시작일';
COMMENT ON COLUMN SHELTER_ANIMALS.public_notice_end IS '공고종료일';
COMMENT ON COLUMN SHELTER_ANIMALS.image_url IS '공공 API 이미지 URL';
COMMENT ON COLUMN SHELTER_ANIMALS.api_source IS 'API 출처 구분';
COMMENT ON COLUMN SHELTER_ANIMALS.weight IS '체중(kg)';
COMMENT ON COLUMN SHELTER_ANIMALS.color_cd IS '색상';
COMMENT ON COLUMN SHELTER_ANIMALS.process_state IS '처리상태';

COMMENT ON COLUMN SHELTER.care_reg_no IS '공공 API 보호소 등록번호';
COMMENT ON COLUMN SHELTER.api_sync_enabled IS 'API 동기화 여부 (Y/N)';
COMMENT ON COLUMN SHELTER.last_sync_date IS '마지막 동기화 일시';

commit;

/*================
2025-07-12 수정
USERS 테이블 USER_NAME 제약조건 삭제
=================*/

ALTER TABLE users DROP CONSTRAINT UQ_USERS_NAME;

commit;



/*================
2025-07-13 수정
SHELTER 테이블 AUTH_FILE_DESCRIPTION  컬럼 추가
=================*/

ALTER TABLE SHELTER
ADD AUTH_FILE_DESCRIPTION VARCHAR2(255 BYTE);

COMMENT ON COLUMN SHELTER.AUTH_FILE_DESCRIPTION IS '보호소 첨부파일 기타 설명란';

commit;






/*================
2025-07-14 수정
공공 API 보호소 정보 컬럼 추가
=================*/

-- 공공 API 보호소 정보 컬럼 추가
ALTER TABLE SHELTER_ANIMALS ADD (
    API_SHELTER_NAME VARCHAR2(200),
    API_SHELTER_TEL VARCHAR2(50),
    API_SHELTER_ADDR VARCHAR2(500),
    API_ORG_NM VARCHAR2(200)
);

-- 컬럼 설명 추가
COMMENT ON COLUMN SHELTER_ANIMALS.API_SHELTER_NAME IS '공공 API 보호소명 (FK 매핑 전 임시 저장)';
COMMENT ON COLUMN SHELTER_ANIMALS.API_SHELTER_TEL IS '공공 API 보호소 전화번호';
COMMENT ON COLUMN SHELTER_ANIMALS.API_SHELTER_ADDR IS '공공 API 보호소 주소';
COMMENT ON COLUMN SHELTER_ANIMALS.API_ORG_NM IS '공공 API 관할기관관';

-- 인덱스 생성성
CREATE INDEX IDX_SHELTER_ANIMALS_PROCESS_STATE ON SHELTER_ANIMALS(PROCESS_STATE);
CREATE INDEX IDX_SHELTER_ANIMALS_API_SHELTER ON SHELTER_ANIMALS(API_SHELTER_NAME);

-- 통계 정보 갱신
EXEC DBMS_STATS.GATHER_TABLE_STATS('DUOPET', 'SHELTER_ANIMALS');

-- SHELTER_ANIMALS SHELTER_ID 컬럼 NULL 가능조건 추가가
ALTER TABLE DUOPET.SHELTER_ANIMALS MODIFY SHELTER_ID NULL;

-- ANIMAL_HOSPITALS 테이블 생성
CREATE TABLE ANIMAL_HOSPITALS (
    hospital_id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    management_no VARCHAR2(50) UNIQUE,
    business_name VARCHAR2(200) NOT NULL,
    road_address VARCHAR2(500),
    jibun_address VARCHAR2(500),
    phone VARCHAR2(20),
    road_postal_code VARCHAR2(10),
    jibun_postal_code VARCHAR2(10),
    latitude NUMBER(10,7),
    longitude NUMBER(10,7),
    epsg5174_x VARCHAR2(50),
    epsg5174_y VARCHAR2(50),
    business_status VARCHAR2(20),
    business_status_code VARCHAR2(10),
    detailed_status VARCHAR2(20),
    detailed_status_code VARCHAR2(10),
    license_date DATE,
    closed_date DATE,
    suspended_start_date DATE,
    suspended_end_date DATE,
    reopened_date DATE,
    city VARCHAR2(50),
    district VARCHAR2(50),
    area_size VARCHAR2(100),
    employee_count NUMBER,
    data_source VARCHAR2(50) DEFAULT '공공데이터포털',
    data_update_type VARCHAR2(10),
    data_update_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 인덱스 생성
CREATE INDEX idx_business_status ON ANIMAL_HOSPITALS(business_status);
CREATE INDEX idx_city_district ON ANIMAL_HOSPITALS(city, district);
CREATE INDEX idx_coordinates ON ANIMAL_HOSPITALS(latitude, longitude);

-- 테이블 코멘트
COMMENT ON TABLE ANIMAL_HOSPITALS IS '동물병원 정보';
COMMENT ON COLUMN ANIMAL_HOSPITALS.hospital_id IS '병원 ID';
COMMENT ON COLUMN ANIMAL_HOSPITALS.management_no IS '관리번호';
COMMENT ON COLUMN ANIMAL_HOSPITALS.business_name IS '사업장명';
COMMENT ON COLUMN ANIMAL_HOSPITALS.road_address IS '도로명주소';
COMMENT ON COLUMN ANIMAL_HOSPITALS.jibun_address IS '지번주소';
COMMENT ON COLUMN ANIMAL_HOSPITALS.phone IS '전화번호';
COMMENT ON COLUMN ANIMAL_HOSPITALS.road_postal_code IS '도로명우편번호';
COMMENT ON COLUMN ANIMAL_HOSPITALS.jibun_postal_code IS '지번우편번호';
COMMENT ON COLUMN ANIMAL_HOSPITALS.latitude IS '위도 (WGS84)';
COMMENT ON COLUMN ANIMAL_HOSPITALS.longitude IS '경도 (WGS84)';
COMMENT ON COLUMN ANIMAL_HOSPITALS.epsg5174_x IS '원본 좌표X (EPSG5174)';
COMMENT ON COLUMN ANIMAL_HOSPITALS.epsg5174_y IS '원본 좌표Y (EPSG5174)';
COMMENT ON COLUMN ANIMAL_HOSPITALS.business_status IS '영업상태명';
COMMENT ON COLUMN ANIMAL_HOSPITALS.business_status_code IS '영업상태구분코드';
COMMENT ON COLUMN ANIMAL_HOSPITALS.detailed_status IS '상세영업상태명';
COMMENT ON COLUMN ANIMAL_HOSPITALS.detailed_status_code IS '상세영업상태코드';
COMMENT ON COLUMN ANIMAL_HOSPITALS.license_date IS '인허가일자';
COMMENT ON COLUMN ANIMAL_HOSPITALS.closed_date IS '폐업일자';
COMMENT ON COLUMN ANIMAL_HOSPITALS.suspended_start_date IS '휴업시작일자';
COMMENT ON COLUMN ANIMAL_HOSPITALS.suspended_end_date IS '휴업종료일자';
COMMENT ON COLUMN ANIMAL_HOSPITALS.reopened_date IS '재개업일자';
COMMENT ON COLUMN ANIMAL_HOSPITALS.city IS '시도명';
COMMENT ON COLUMN ANIMAL_HOSPITALS.district IS '시군구명';
COMMENT ON COLUMN ANIMAL_HOSPITALS.area_size IS '소재지면적';
COMMENT ON COLUMN ANIMAL_HOSPITALS.employee_count IS '총직원수';
COMMENT ON COLUMN ANIMAL_HOSPITALS.data_source IS '데이터 출처';
COMMENT ON COLUMN ANIMAL_HOSPITALS.data_update_type IS '데이터갱신구분';
COMMENT ON COLUMN ANIMAL_HOSPITALS.data_update_date IS '데이터갱신일자';
COMMENT ON COLUMN ANIMAL_HOSPITALS.created_at IS '생성일시';
COMMENT ON COLUMN ANIMAL_HOSPITALS.updated_at IS '수정일시';

/*================
2025-07-15 수정
USERS 테이블 USER_PWD, GENDER, ADDRESS null 허용
=================*/

ALTER TABLE USERS MODIFY USER_PWD VARCHAR2(255) NULL;
ALTER TABLE USERS MODIFY GENDER NULL;
ALTER TABLE USERS MODIFY ADDRESS  VARCHAR2(255) NULL;
commit;


/*================
2025-07-16 테이블 추가
REPORT 테이블 추가 
=================*/

CREATE TABLE REPORT (
    report_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       NUMBER        NOT NULL,
    target_id     NUMBER        NOT NULL,
    target_type   VARCHAR2(50)  NOT NULL,
    reason        VARCHAR2(1000),
    status        VARCHAR2(20)  DEFAULT 'PENDING' NOT NULL,
    created_at    DATE          DEFAULT SYSDATE NOT NULL,

    CONSTRAINT fk_report_user    FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_report_type   CHECK (target_type IN ('content', 'comment', 'review')),
    CONSTRAINT chk_report_status CHECK (status IN ('PENDING', 'REVIEWED', 'BLOCKED'))
);

-- 테이블 코멘트

COMMENT ON TABLE report                           IS '신고 정보';
COMMENT ON COLUMN report.report_id               IS 'PK, IDENTITY (신고 고유 식별자)';
COMMENT ON COLUMN report.user_id                 IS 'FK (USERS 테이블의 user_id 참조), 신고자 고유 식별자';
COMMENT ON COLUMN report.target_id               IS '신고 대상의 고유 ID (게시글, 댓글, 리뷰 등)';
COMMENT ON COLUMN report.target_type             IS '신고 대상 유형 (content, comment, review)';
COMMENT ON COLUMN report.reason                  IS '사용자가 작성한 신고 이유';
COMMENT ON COLUMN report.status                  IS '신고 처리 상태 (PENDING, REVIEWED, BLOCKED)';
COMMENT ON COLUMN report.created_at              IS '신고 생성일 (SYSDATE)';

commit;

/*================
2025-07-16 제약 조건 변경
bookmark 테이블 TARGET_TYPE 제약조건 '자유'에서 null로 변경 
제약조건 삭제
=================*/

ALTER TABLE BOOKMARK 
  MODIFY TARGET_TYPE VARCHAR2(50 BYTE) NOT NULL;
  
  ALTER TABLE BOOKMARK 
  MODIFY TARGET_TYPE DEFAULT NULL;
  
  ALTER TABLE BOOKMARK DROP CONSTRAINT CK_BM_TYPE;
  
  commit;

/*================
2025-07-16 컬럼 추가
content 테이블에 컬럼명 bookmarkCount 삭제 및 
bookmark_Count 컬럼 추가
=================*/

ALTER TABLE content DROP COLUMN BOOKMARKCOUNT;

ALTER TABLE content
ADD BOOKMARK_COUNT NUMBER DEFAULT 0;

COMMENT ON COLUMN content.BOOKMARK_COUNT IS '게시글의 북마크 수';

commit;
=======

/*================
2025-07-16 수정
FAQ 추가
=================*/
-- 1. 사료 교체 주기
INSERT INTO faq (user_id, question, answer) VALUES (1, '사료는 얼마나 자주 교체해야 하나요?', '일반적으로 4-6개월 간격으로 단계적으로 교체하며, 장 트러블 여부를 관찰하세요. 특정 연령이나 건강 상태에 따라 수의사와 상담 후 결정하는 것이 가장 좋습니다.');

-- 2. 사람 음식 급여
INSERT INTO faq (user_id, question, answer) VALUES (1, '강아지나 고양이에게 사람 음식을 줘도 되나요?', '가급적 주지 않는 것이 좋습니다. 특히 초콜릿, 양파, 마늘, 포도 등은 반려동물에게 매우 치명적일 수 있습니다. 전용 간식을 급여해 주세요.');

-- 3. 예방접종
INSERT INTO faq (user_id, question, answer) VALUES (1, '예방접종은 꼭 해야 하나요?', '네, 반드시 필요합니다. 예방접종은 전염병으로부터 반려동물의 생명을 지키는 가장 효과적인 방법입니다. 동물병원에서 권장하는 일정에 맞춰 접종을 완료해 주세요.');

-- 4. 중성화 수술
INSERT INTO faq (user_id, question, answer) VALUES (1, '중성화 수술은 언제 하는 것이 좋은가요?', '일반적으로 생후 6개월 전후에 권장되지만, 품종과 건강 상태에 따라 달라질 수 있습니다. 수의사와 상담하여 최적의 시기를 결정하는 것이 중요합니다.');

-- 5. 심장사상충 예방
INSERT INTO faq (user_id, question, answer) VALUES (1, '심장사상충 예방은 매달 해야 하나요?', '네, 심장사상충은 모기를 통해 감염되며 치사율이 높은 질병입니다. 한 달에 한 번씩 정기적으로 예방약을 투여하여 꾸준히 관리해야 합니다.');

-- 6. 배변 훈련
INSERT INTO faq (user_id, question, answer) VALUES (1, '배변 훈련은 어떻게 시작해야 하나요?', '일정한 장소에 배변패드를 깔아두고, 성공했을 때 즉시 칭찬과 보상을 해주는 긍정 강화 훈련이 효과적입니다. 인내심을 갖고 꾸준히 반복해 주세요.');

-- 7. 사회화 시기
INSERT INTO faq (user_id, question, answer) VALUES (1, '강아지의 사회화 시기는 언제가 중요한가요?', '생후 3주에서 16주 사이가 사회성을 기르는 가장 중요한 시기입니다. 이 시기에 다른 강아지, 사람, 다양한 소리 및 환경을 안전하게 경험하게 해주세요.');

-- 8. 분리불안
INSERT INTO faq (user_id, question, answer) VALUES (1, '분리불안 증상은 무엇이고 어떻게 대처하나요?', '보호자가 없을 때 불안을 느껴 과도하게 짖거나 하울링, 파괴적인 행동을 보이는 증상입니다. 짧은 외출부터 시작해 혼자 있는 시간을 점진적으로 늘리는 훈련이 도움이 될 수 있습니다.');

-- 9. 목욕 주기
INSERT INTO faq (user_id, question, answer) VALUES (1, '목욕은 얼마나 자주 시켜야 하나요?', '견종과 생활 환경에 따라 다르지만, 보통 한 달에 1~2회 정도가 적당합니다. 너무 잦은 목욕은 피부를 건조하게 만들 수 있으니 주의하세요.');

-- 10. 털 관리
INSERT INTO faq (user_id, question, answer) VALUES (1, '털이 너무 많이 빠져요. 어떻게 관리해야 하나요?', '매일 꾸준히 빗질을 해주는 것이 가장 중요합니다. 죽은 털을 제거하고 피부 혈액순환을 도와줍니다. 오메가-3 같은 영양제 급여도 도움이 될 수 있습니다.');

-- 11. 구강 관리
INSERT INTO faq (user_id, question, answer) VALUES (1, '치아 관리는 어떻게 해주어야 하나요?', '매일 양치질을 해주는 것이 가장 이상적입니다. 어렵다면 덴탈껌이나 장난감을 활용하고, 1년에 한 번씩 동물병원에서 스케일링 상담을 받는 것을 추천합니다.');

-- 12. 발톱 관리
INSERT INTO faq (user_id, question, answer) VALUES (1, '발톱은 얼마나 자주 깎아야 하나요?', '반려동물이 걸을 때 바닥에 발톱 닿는 소리가 들린다면 깎아줄 때가 된 것입니다. 보통 2~4주에 한 번씩 관리해주는 것이 좋으며, 혈관이 다치지 않도록 주의해야 합니다.');

-- 13. 사료 알러지
INSERT INTO faq (user_id, question, answer) VALUES (1, '사료 알러지 증상은 무엇인가요?', '주요 증상으로는 피부 가려움증, 붉어짐, 귀의 염증, 잦은 설사나 구토 등이 있습니다. 특정 단백질원에 반응하는 경우가 많아 수의사와 상담 후 가수분해 사료 등을 급여해 볼 수 있습니다.');

-- 14. 내장칩(마이크로칩)
INSERT INTO faq (user_id, question, answer) VALUES (1, '내장칩(마이크로칩)은 꼭 등록해야 하나요?', '네, 현행법상 동물등록은 의무사항입니다. 내장칩 시술 후 반드시 시·군·구청에 보호자 정보를 등록해야 과태료가 부과되지 않으며, 반려동물을 잃어버렸을 때 찾을 확률이 크게 높아집니다.');

-- 15. 건강 이상 신호
INSERT INTO faq (user_id, question, answer) VALUES (1, '반려동물이 아플 때 나타나는 이상 신호는 무엇인가요?', '식욕 부진, 기력 저하, 구토, 설사, 호흡 곤란, 평소와 다른 공격성이나 숨는 행동 등이 있다면 건강 이상 신호일 수 있습니다. 즉시 동물병원에 내원하세요.');

-- 16. 정기 건강검진
INSERT INTO faq (user_id, question, answer) VALUES (1, '정기 건강검진은 얼마나 자주 받아야 하나요?', '성견/성묘는 1년에 한 번, 7세 이상의 노령견/노령묘는 6개월에 한 번씩 받는 것을 권장합니다. 질병을 조기에 발견하고 치료하는 데 큰 도움이 됩니다.');

-- 17. 고양이 스크래치
INSERT INTO faq (user_id, question, answer) VALUES (1, '고양이가 자꾸 가구를 긁어요. 어떻게 하죠?', '스크래칭은 고양이의 자연스러운 본능입니다. 다양한 재질의 스크래처를 집안 곳곳에 비치해주고, 가구에는 고양이가 싫어하는 향의 스프레이를 뿌려두는 것이 도움이 됩니다.');

-- 18. 적정 산책 횟수
INSERT INTO faq (user_id, question, answer) VALUES (1, '강아지 산책은 하루에 몇 번이 적당한가요?', '견종의 에너지 수준에 따라 다르지만, 일반적으로 하루에 최소 2번, 각 30분 이상을 권장합니다. 산책은 에너지 소비뿐만 아니라 스트레스 해소와 사회화에 매우 중요합니다.');

-- 19. 안전한 장난감
INSERT INTO faq (user_id, question, answer) VALUES (1, '안전한 장난감을 고르는 기준이 있나요?', '반려동물이 삼킬 수 없는 크기여야 하고, 쉽게 부서지거나 날카로운 조각이 생기지 않는 튼튼한 재질로 만들어진 제품을 선택해야 합니다.');

-- 20. 반려동물 분실 시 대처법
INSERT INTO faq (user_id, question, answer) VALUES (1, '반려동물을 잃어버렸을 때 어떻게 해야 하나요?', '즉시 잃어버린 장소 주변을 수색하고, 동물보호관리시스템 및 지역 커뮤니티(SNS, 맘카페 등)에 실종 신고 글을 올리세요. 인근 동물병원과 보호소에도 연락해두는 것이 좋습니다.');

COMMIT;

/*================
2025-07-17 
Report 테이블
DETAILS 컬럼 추가 및 REASON 데이터 타입 값 수정
=================*/

ALTER TABLE REPORT
MODIFY REASON VARCHAR2(30);

ALTER TABLE REPORT
ADD DETAILS VARCHAR2(1000);

COMMENT ON COLUMN REPORT.DETAILS IS '신고에 대한 상세 설명 (선택사항)';

commit;

/*================
2025-07-18
Users 테이블
suspended_until 컬럼 추가
=================*/

ALTER TABLE USERS ADD (suspended_until TIMESTAMP);
COMMENT ON COLUMN USERS.suspended_until IS '사용자 정지 만료 시간';

commit;

/*================
2025-07-18
SELTER_INFO 테이블 생성성
SELTER 테이블 수정정
=================*/

-- SHELTER_INFO 테이블 생성
CREATE TABLE SHELTER_INFO (
    shelter_info_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    care_reg_no        VARCHAR2(50) NOT NULL UNIQUE,
    care_nm            VARCHAR2(200) NOT NULL,
    org_nm             VARCHAR2(200),
    division_nm        VARCHAR2(50),
    save_trgt_animal   VARCHAR2(100),
    care_addr          VARCHAR2(500),
    jibun_addr         VARCHAR2(500),
    lat                NUMBER(10,7),
    lng                NUMBER(10,7),
    care_tel           VARCHAR2(50),
    dsignation_date    DATE,
    week_opr_stime     VARCHAR2(10),
    week_opr_etime     VARCHAR2(10),
    close_day          VARCHAR2(100),
    vet_person_cnt     NUMBER,
    specs_person_cnt   NUMBER,
    data_std_dt        DATE,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_shelter_info_location ON SHELTER_INFO(lat, lng);
CREATE INDEX idx_shelter_info_org ON SHELTER_INFO(org_nm);
CREATE INDEX idx_shelter_info_status ON SHELTER_INFO(division_nm);
CREATE INDEX idx_shelter_info_animal ON SHELTER_INFO(save_trgt_animal);
CREATE INDEX idx_shelter_info_region ON SHELTER_INFO(care_addr);

-- 코멘트 추가
COMMENT ON TABLE SHELTER_INFO IS '공공데이터 API로부터 수집한 보호소 정보';
COMMENT ON COLUMN SHELTER_INFO.shelter_info_id IS '보호소 정보 ID (내부 PK)';
COMMENT ON COLUMN SHELTER_INFO.care_reg_no IS '관리번호 (공공데이터 고유값)';
COMMENT ON COLUMN SHELTER_INFO.care_nm IS '보호소명';
COMMENT ON COLUMN SHELTER_INFO.org_nm IS '관할기관';
COMMENT ON COLUMN SHELTER_INFO.division_nm IS '보호소구분 (법인/개인/단체/기타)';
COMMENT ON COLUMN SHELTER_INFO.save_trgt_animal IS '보호동물 종류';
COMMENT ON COLUMN SHELTER_INFO.care_addr IS '도로명주소';
COMMENT ON COLUMN SHELTER_INFO.jibun_addr IS '지번주소';
COMMENT ON COLUMN SHELTER_INFO.lat IS '위도 (WGS84)';
COMMENT ON COLUMN SHELTER_INFO.lng IS '경도 (WGS84)';
COMMENT ON COLUMN SHELTER_INFO.care_tel IS '전화번호';
COMMENT ON COLUMN SHELTER_INFO.dsignation_date IS '지정일자';
COMMENT ON COLUMN SHELTER_INFO.week_opr_stime IS '평일운영시작시간';
COMMENT ON COLUMN SHELTER_INFO.week_opr_etime IS '평일운영종료시간';
COMMENT ON COLUMN SHELTER_INFO.close_day IS '휴무일';
COMMENT ON COLUMN SHELTER_INFO.vet_person_cnt IS '수의사수';
COMMENT ON COLUMN SHELTER_INFO.specs_person_cnt IS '사양관리사수';
COMMENT ON COLUMN SHELTER_INFO.data_std_dt IS '데이터기준일자';

-- SHELTER 테이블 수정
ALTER TABLE SHELTER ADD shelter_info_id NUMBER;
ALTER TABLE SHELTER MODIFY shelter_name VARCHAR2(100) NULL;
ALTER TABLE SHELTER MODIFY phone VARCHAR2(20) NULL;
ALTER TABLE SHELTER MODIFY address VARCHAR2(255) NULL;

-- 외래키 제약조건 추가
ALTER TABLE SHELTER ADD CONSTRAINT fk_shelter_info
    FOREIGN KEY (shelter_info_id) REFERENCES SHELTER_INFO(shelter_info_id);

-- SHELTER 테이블 컬럼 코멘트
COMMENT ON COLUMN SHELTER.shelter_info_id IS '공공데이터 보호소 정보 참조 (NULL 가능)';
COMMENT ON COLUMN SHELTER.shelter_name IS '보호소명 (shelter_info_id가 있으면 SHELTER_INFO에서 가져옴)';
COMMENT ON COLUMN SHELTER.phone IS '전화번호 (shelter_info_id가 있으면 SHELTER_INFO에서 가져옴)';
COMMENT ON COLUMN SHELTER.address IS '주소 (shelter_info_id가 있으면 SHELTER_INFO에서 가져옴)';

-- 트리거 생성 (updated_at 자동 업데이트)
CREATE OR REPLACE TRIGGER trg_shelter_info_updated_at
BEFORE UPDATE ON SHELTER_INFO
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- 보호소 매칭 테이블 (선택사항)
CREATE TABLE SHELTER_MAPPING (
    mapping_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    shelter_id         NUMBER NOT NULL,
    shelter_info_id    NUMBER NOT NULL,
    match_type         VARCHAR2(20) DEFAULT 'auto',
    match_score        NUMBER(3,2),
    matched_by         VARCHAR2(100),
    matched_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mapping_shelter FOREIGN KEY (shelter_id) REFERENCES SHELTER(shelter_id),
    CONSTRAINT fk_mapping_shelter_info FOREIGN KEY (shelter_info_id) REFERENCES SHELTER_INFO(shelter_info_id),
    CONSTRAINT uq_shelter_mapping UNIQUE (shelter_id, shelter_info_id)
);

COMMENT ON TABLE SHELTER_MAPPING IS '회원 보호소와 공공데이터 보호소 매칭 기록';
COMMENT ON COLUMN SHELTER_MAPPING.match_type IS '매칭 방식 (auto: 자동, manual: 수동)';
COMMENT ON COLUMN SHELTER_MAPPING.match_score IS '매칭 신뢰도 (0~1)';

commit;

/*================
2025-07-21

USERS 테이블 컬럼 추가
FACE_ORIGINAL_FILENAME , FACE_RENAME_FILENAME
=================*/

ALTER TABLE USERS ADD FACE_ORIGINAL_FILENAME VARCHAR2(255);
ALTER TABLE USERS ADD FACE_RENAME_FILENAME VARCHAR2(255);

COMMENT ON COLUMN USERS.FACE_ORIGINAL_FILENAME IS '회원 프로필 이미지의 원본 파일명';
COMMENT ON COLUMN USERS.FACE_RENAME_FILENAME IS '회원 페이스 이미지의 서버 저장 파일명';

commit;


/*================
2025-07-21
PET_HEALTH_RECORD 테이블 생성
PET_HEALTH_FILE 테이블 생성
=================*/

-- PET_HEALTH_RECORD 테이블 생성
-- 시퀀스 생성 (Oracle 11g 호환)
CREATE SEQUENCE PET_HEALTH_RECORD_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- PET_HEALTH_RECORD 테이블 생성
CREATE TABLE PET_HEALTH_RECORD (
    record_id NUMBER(19) PRIMARY KEY,
    pet_id NUMBER(19) NOT NULL,
    record_type VARCHAR2(50) NOT NULL,
    title VARCHAR2(200) NOT NULL,
    record_date DATE NOT NULL,
    veterinarian VARCHAR2(100),
    content CLOB,
    status VARCHAR2(20) DEFAULT '완료',
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_pet_health_record_pet FOREIGN KEY (pet_id) REFERENCES PET(pet_id)
);

-- PET_HEALTH_RECORD 테이블 주석
COMMENT ON TABLE PET_HEALTH_RECORD IS '반려동물 건강 기록 테이블';
COMMENT ON COLUMN PET_HEALTH_RECORD.record_id IS '건강기록 ID';
COMMENT ON COLUMN PET_HEALTH_RECORD.pet_id IS '반려동물 ID';
COMMENT ON COLUMN PET_HEALTH_RECORD.record_type IS '기록 유형 (정기검진, 예방접종, 치료, 응급처치, 기타)';
COMMENT ON COLUMN PET_HEALTH_RECORD.title IS '제목';
COMMENT ON COLUMN PET_HEALTH_RECORD.record_date IS '기록 날짜';
COMMENT ON COLUMN PET_HEALTH_RECORD.veterinarian IS '담당 수의사';
COMMENT ON COLUMN PET_HEALTH_RECORD.content IS '상세 내용';
COMMENT ON COLUMN PET_HEALTH_RECORD.status IS '상태 (완료, 예정)';
COMMENT ON COLUMN PET_HEALTH_RECORD.created_at IS '생성일시';
COMMENT ON COLUMN PET_HEALTH_RECORD.updated_at IS '수정일시';

-- PET_HEALTH_FILE 시퀀스 생성
CREATE SEQUENCE PET_HEALTH_FILE_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- PET_HEALTH_FILE 테이블 생성
CREATE TABLE PET_HEALTH_FILE (
    file_id NUMBER(19) PRIMARY KEY,
    record_id NUMBER(19) NOT NULL,
    original_filename VARCHAR2(255) NOT NULL,
    stored_filename VARCHAR2(255) NOT NULL,
    file_size NUMBER(19) NOT NULL,
    content_type VARCHAR2(100) NOT NULL,
    file_path VARCHAR2(500) NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_pet_health_file_record FOREIGN KEY (record_id) REFERENCES PET_HEALTH_RECORD(record_id) ON DELETE CASCADE
);

-- PET_HEALTH_FILE 테이블 주석
COMMENT ON TABLE PET_HEALTH_FILE IS '반려동물 건강 기록 첨부 파일 테이블';
COMMENT ON COLUMN PET_HEALTH_FILE.file_id IS '파일 ID';
COMMENT ON COLUMN PET_HEALTH_FILE.record_id IS '건강기록 ID';
COMMENT ON COLUMN PET_HEALTH_FILE.original_filename IS '원본 파일명';
COMMENT ON COLUMN PET_HEALTH_FILE.stored_filename IS '저장된 파일명';
COMMENT ON COLUMN PET_HEALTH_FILE.file_size IS '파일 크기';
COMMENT ON COLUMN PET_HEALTH_FILE.content_type IS '파일 타입';
COMMENT ON COLUMN PET_HEALTH_FILE.file_path IS '파일 저장 경로';
COMMENT ON COLUMN PET_HEALTH_FILE.created_at IS '생성일시';

-- PET_HEALTH_RECORD 트리거 생성 (INSERT 시 ID 자동 생성)
CREATE OR REPLACE TRIGGER PET_HEALTH_RECORD_BI_TRG
BEFORE INSERT ON PET_HEALTH_RECORD
FOR EACH ROW
BEGIN
    IF :NEW.record_id IS NULL THEN
        SELECT PET_HEALTH_RECORD_SEQ.NEXTVAL INTO :NEW.record_id FROM DUAL;
    END IF;
    :NEW.created_at := SYSTIMESTAMP;
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- PET_HEALTH_RECORD 트리거 생성 (UPDATE 시 updated_at 자동 갱신)
CREATE OR REPLACE TRIGGER PET_HEALTH_RECORD_BU_TRG
BEFORE UPDATE ON PET_HEALTH_RECORD
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

-- PET_HEALTH_FILE 트리거 생성 (INSERT 시 ID 자동 생성)
CREATE OR REPLACE TRIGGER PET_HEALTH_FILE_BI_TRG
BEFORE INSERT ON PET_HEALTH_FILE
FOR EACH ROW
BEGIN
    IF :NEW.file_id IS NULL THEN
        SELECT PET_HEALTH_FILE_SEQ.NEXTVAL INTO :NEW.file_id FROM DUAL;
    END IF;
    :NEW.created_at := SYSTIMESTAMP;
END;
/

-- 인덱스 생성 (성능 향상)
CREATE INDEX idx_pet_health_record_pet_id ON PET_HEALTH_RECORD(pet_id);
CREATE INDEX idx_pet_health_record_date ON PET_HEALTH_RECORD(record_date);
CREATE INDEX idx_pet_health_file_record_id ON PET_HEALTH_FILE(record_id);

-- 권한 부여 (필요 시)
-- GRANT ALL ON PET_HEALTH_RECORD TO duopet;
-- GRANT ALL ON PET_HEALTH_FILE TO duopet;
-- GRANT ALL ON PET_HEALTH_RECORD_SEQ TO duopet;
-- GRANT ALL ON PET_HEALTH_FILE_SEQ TO duopet;

COMMIT;


/*================
2025-07-21
PET_VACCIN 테이블 수정
병원명 컬럼 추가
=================*/
ALTER TABLE pet_vaccin
ADD hospital_name VARCHAR2(100);

COMMENT ON COLUMN pet_vaccin.hospital_name IS '예방접종을 실시한 병원명';

COMMIT;

  -- 건강 기록 테이블
  
ALTER TABLE PET_HEALTH_RECORD
ADD hospital_name VARCHAR2(100);

COMMENT ON COLUMN PET_HEALTH_RECORD.hospital_name IS '병원명';
  
/*================
2025-07-21
COMMENTS 테이블 LIKE_COUNT , REPORT_COUNT 컬럼 추가 
=================*/

ALTER TABLE COMMENTS
ADD LIKE_COUNT NUMBER DEFAULT 0 NOT NULL;

COMMENT ON COLUMN COMMENTS.LIKE_COUNT IS '댓글의 좋아요 수';

ALTER TABLE COMMENTS
ADD REPORT_COUNT NUMBER DEFAULT 0 NOT NULL;

COMMENT ON COLUMN COMMENTS.REPORT_COUNT IS '댓글의 신고 횟수';

COMMIT;

/*================
2025-07-21
REPORT 테이블 SUSPENDED_UNTIL 컬럼 삭제 
=================*/

ALTER TABLE REPORT DROP COLUMN SUSPENDED_UNTIL;


COMMIT;


/*==============================================================
  수의사 상담 기능 데이터베이스 스키마
  생성일: 2025-07-22
  설명: 기존 VET 테이블을 활용한 실시간 채팅 상담 기능
  
  주의사항:
  - 기존 VET 테이블은 수정하지 않음
  - VET_PROFILE로 상담 관련 추가 정보만 관리
  - 모든 테이블은 기존 스키마와 호환되도록 설계
==============================================================*/

-- 기존 테이블 삭제 (필요시 백업 후 실행)
DROP TABLE chat_message CASCADE CONSTRAINTS;
DROP TABLE consultation_review CASCADE CONSTRAINTS;
DROP TABLE consultation_room CASCADE CONSTRAINTS;
DROP TABLE vet_schedule CASCADE CONSTRAINTS;
DROP TABLE vet_profile CASCADE CONSTRAINTS;

/*==============================================================
  1. VET_PROFILE (수의사 상담 프로필 - VET 테이블 확장)
==============================================================*/
CREATE TABLE vet_profile (
    profile_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vet_id              NUMBER NOT NULL,
    introduction        CLOB,
    consultation_fee    NUMBER(10,2) DEFAULT 30000,
    is_available        CHAR(1) DEFAULT 'Y' NOT NULL,
    is_online           CHAR(1) DEFAULT 'N' NOT NULL,
    last_online_at      DATE,
    rating_avg          NUMBER(3,2) DEFAULT 0,
    rating_count        NUMBER DEFAULT 0,
    consultation_count  NUMBER DEFAULT 0,
    response_time_avg   NUMBER DEFAULT 0,  -- 평균 응답 시간(분)
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    updated_at          DATE,
    CONSTRAINT fk_vp_vet FOREIGN KEY (vet_id) REFERENCES vet(vet_id),
    CONSTRAINT uq_vp_vet UNIQUE (vet_id),
    CONSTRAINT ck_vp_available CHECK (is_available IN ('Y', 'N')),
    CONSTRAINT ck_vp_online CHECK (is_online IN ('Y', 'N'))
);

COMMENT ON TABLE vet_profile IS '수의사 상담 프로필 (상담 서비스 추가 정보)';
COMMENT ON COLUMN vet_profile.profile_id IS 'PK, 프로필 고유 ID';
COMMENT ON COLUMN vet_profile.vet_id IS 'FK, VET 테이블 참조';
COMMENT ON COLUMN vet_profile.introduction IS '상담 전문가 소개글';
COMMENT ON COLUMN vet_profile.consultation_fee IS '기본 상담료 (30분 기준)';
COMMENT ON COLUMN vet_profile.is_available IS '상담 서비스 제공 여부';
COMMENT ON COLUMN vet_profile.is_online IS '현재 온라인 상태';
COMMENT ON COLUMN vet_profile.last_online_at IS '마지막 접속 시간';
COMMENT ON COLUMN vet_profile.rating_avg IS '평균 평점 (5점 만점)';
COMMENT ON COLUMN vet_profile.rating_count IS '평가 횟수';
COMMENT ON COLUMN vet_profile.consultation_count IS '총 상담 횟수';
COMMENT ON COLUMN vet_profile.response_time_avg IS '평균 응답 시간(분)';

/*==============================================================
  2. VET_SCHEDULE (수의사 상담 가능 일정)
==============================================================*/
CREATE TABLE vet_schedule (
    schedule_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vet_id              NUMBER NOT NULL,
    schedule_date       DATE NOT NULL,
    start_time          VARCHAR2(5) NOT NULL,  -- 'HH:MI'
    end_time            VARCHAR2(5) NOT NULL,
    max_consultations   NUMBER DEFAULT 10,      -- 시간대별 최대 상담 수
    current_bookings    NUMBER DEFAULT 0,       -- 현재 예약 수
    is_available        CHAR(1) DEFAULT 'Y' NOT NULL,
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    updated_at          DATE,
    CONSTRAINT fk_vs_vet FOREIGN KEY (vet_id) REFERENCES vet(vet_id),
    CONSTRAINT ck_vs_available CHECK (is_available IN ('Y', 'N')),
    CONSTRAINT ck_vs_time CHECK (start_time < end_time)
);

COMMENT ON TABLE vet_schedule IS '수의사 상담 가능 일정';
COMMENT ON COLUMN vet_schedule.schedule_id IS 'PK, 일정 고유 ID';
COMMENT ON COLUMN vet_schedule.vet_id IS 'FK, VET 테이블 참조';
COMMENT ON COLUMN vet_schedule.schedule_date IS '상담 가능 날짜';
COMMENT ON COLUMN vet_schedule.start_time IS '시작 시간 (HH:MI)';
COMMENT ON COLUMN vet_schedule.end_time IS '종료 시간 (HH:MI)';
COMMENT ON COLUMN vet_schedule.max_consultations IS '시간대별 최대 상담 수';
COMMENT ON COLUMN vet_schedule.current_bookings IS '현재 예약된 상담 수';

/*==============================================================
  3. CONSULTATION_ROOM (상담방)
==============================================================*/
CREATE TABLE consultation_room (
    room_id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_uuid           VARCHAR2(36) DEFAULT SYS_GUID() NOT NULL,
    user_id             NUMBER NOT NULL,
    vet_id              NUMBER NOT NULL,
    pet_id              NUMBER,
    schedule_id         NUMBER,
    room_status         VARCHAR2(20) DEFAULT 'CREATED' NOT NULL,
    consultation_type   VARCHAR2(20) DEFAULT 'CHAT' NOT NULL,
    scheduled_datetime  DATE,
    started_at          DATE,
    ended_at            DATE,
    duration_minutes    NUMBER,
    consultation_fee    NUMBER(10,2),
    payment_status      VARCHAR2(20) DEFAULT 'PENDING',
    payment_method      VARCHAR2(20),
    paid_at             DATE,
    chief_complaint     CLOB,        -- 주 증상
    consultation_notes  CLOB,        -- 상담 내용 요약
    prescription        CLOB,        -- 처방/권고사항
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    updated_at          DATE,
    CONSTRAINT fk_cr_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_cr_vet FOREIGN KEY (vet_id) REFERENCES vet(vet_id),
    CONSTRAINT fk_cr_pet FOREIGN KEY (pet_id) REFERENCES pet(pet_id),
    CONSTRAINT fk_cr_schedule FOREIGN KEY (schedule_id) REFERENCES vet_schedule(schedule_id),
    CONSTRAINT ck_cr_status CHECK (room_status IN ('CREATED', 'WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT ck_cr_type CHECK (consultation_type IN ('CHAT', 'VIDEO', 'PHONE')),
    CONSTRAINT ck_cr_payment CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'FAILED'))
);

COMMENT ON TABLE consultation_room IS '상담방 정보';
COMMENT ON COLUMN consultation_room.room_id IS 'PK, 상담방 고유 ID';
COMMENT ON COLUMN consultation_room.room_uuid IS '상담방 UUID (URL용)';
COMMENT ON COLUMN consultation_room.user_id IS 'FK, 상담 신청 사용자';
COMMENT ON COLUMN consultation_room.vet_id IS 'FK, 담당 수의사';
COMMENT ON COLUMN consultation_room.pet_id IS 'FK, 상담 대상 반려동물';
COMMENT ON COLUMN consultation_room.schedule_id IS 'FK, 예약 일정';
COMMENT ON COLUMN consultation_room.room_status IS '상담방 상태';
COMMENT ON COLUMN consultation_room.consultation_type IS '상담 유형';
COMMENT ON COLUMN consultation_room.chief_complaint IS '주 증상/상담 사유';
COMMENT ON COLUMN consultation_room.consultation_notes IS '상담 내용 요약';
COMMENT ON COLUMN consultation_room.prescription IS '처방 및 권고사항';

/*==============================================================
  4. CHAT_MESSAGE (채팅 메시지)
==============================================================*/
CREATE TABLE chat_message (
    message_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id             NUMBER NOT NULL,
    sender_id           NUMBER NOT NULL,
    sender_type         VARCHAR2(10) NOT NULL,   -- 'USER', 'VET', 'SYSTEM'
    message_type        VARCHAR2(20) DEFAULT 'TEXT' NOT NULL,
    content             CLOB,
    file_url            VARCHAR2(500),
    file_name           VARCHAR2(255),
    file_size           NUMBER,
    thumbnail_url       VARCHAR2(500),
    is_read             CHAR(1) DEFAULT 'N' NOT NULL,
    read_at             DATE,
    is_important        CHAR(1) DEFAULT 'N' NOT NULL,
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_cm_room FOREIGN KEY (room_id) REFERENCES consultation_room(room_id),
    CONSTRAINT fk_cm_sender FOREIGN KEY (sender_id) REFERENCES users(user_id),
    CONSTRAINT ck_cm_sender_type CHECK (sender_type IN ('USER', 'VET', 'SYSTEM')),
    CONSTRAINT ck_cm_type CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM', 'NOTICE')),
    CONSTRAINT ck_cm_read CHECK (is_read IN ('Y', 'N')),
    CONSTRAINT ck_cm_important CHECK (is_important IN ('Y', 'N'))
);

COMMENT ON TABLE chat_message IS '상담 채팅 메시지';
COMMENT ON COLUMN chat_message.message_id IS 'PK, 메시지 고유 ID';
COMMENT ON COLUMN chat_message.room_id IS 'FK, 상담방 ID';
COMMENT ON COLUMN chat_message.sender_id IS 'FK, 발신자 ID';
COMMENT ON COLUMN chat_message.sender_type IS '발신자 유형';
COMMENT ON COLUMN chat_message.message_type IS '메시지 유형';
COMMENT ON COLUMN chat_message.content IS '메시지 내용';
COMMENT ON COLUMN chat_message.is_important IS '중요 메시지 여부 (처방 등)';

/*==============================================================
  5. CONSULTATION_REVIEW (상담 리뷰)
==============================================================*/
CREATE TABLE consultation_review (
    review_id           NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id             NUMBER NOT NULL,
    user_id             NUMBER NOT NULL,
    vet_id              NUMBER NOT NULL,
    rating              NUMBER(1) NOT NULL,
    kindness_score      NUMBER(1),              -- 친절도
    professional_score  NUMBER(1),              -- 전문성
    response_score      NUMBER(1),              -- 응답속도
    review_content      CLOB,
    vet_reply           CLOB,                   -- 수의사 답변
    is_visible          CHAR(1) DEFAULT 'Y' NOT NULL,
    created_at          DATE DEFAULT SYSDATE NOT NULL,
    updated_at          DATE,
    replied_at          DATE,
    CONSTRAINT fk_crv_room FOREIGN KEY (room_id) REFERENCES consultation_room(room_id),
    CONSTRAINT fk_crv_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_crv_vet FOREIGN KEY (vet_id) REFERENCES vet(vet_id),
    CONSTRAINT ck_crv_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT ck_crv_kindness CHECK (kindness_score BETWEEN 1 AND 5),
    CONSTRAINT ck_crv_professional CHECK (professional_score BETWEEN 1 AND 5),
    CONSTRAINT ck_crv_response CHECK (response_score BETWEEN 1 AND 5),
    CONSTRAINT ck_crv_visible CHECK (is_visible IN ('Y', 'N')),
    CONSTRAINT uq_crv_room UNIQUE (room_id)
);

COMMENT ON TABLE consultation_review IS '상담 리뷰';
COMMENT ON COLUMN consultation_review.review_id IS 'PK, 리뷰 고유 ID';
COMMENT ON COLUMN consultation_review.rating IS '종합 평점 (1-5)';
COMMENT ON COLUMN consultation_review.kindness_score IS '친절도 점수';
COMMENT ON COLUMN consultation_review.professional_score IS '전문성 점수';
COMMENT ON COLUMN consultation_review.response_score IS '응답속도 점수';
COMMENT ON COLUMN consultation_review.vet_reply IS '수의사 답변';

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_vp_online ON vet_profile(is_online, is_available);
CREATE INDEX idx_vs_vet_date ON vet_schedule(vet_id, schedule_date);
CREATE INDEX idx_cr_user ON consultation_room(user_id);
CREATE INDEX idx_cr_vet ON consultation_room(vet_id);
CREATE INDEX idx_cr_status ON consultation_room(room_status);
CREATE INDEX idx_cr_created ON consultation_room(created_at);
CREATE INDEX idx_cm_room ON chat_message(room_id);
CREATE INDEX idx_cm_created ON chat_message(created_at);
CREATE INDEX idx_crv_vet ON consultation_review(vet_id);
CREATE INDEX idx_crv_visible ON consultation_review(is_visible);

-- 시퀀스 (필요시 사용)
-- CREATE SEQUENCE seq_room_id START WITH 1000 INCREMENT BY 1;
-- CREATE SEQUENCE seq_message_id START WITH 1 INCREMENT BY 1;

COMMIT;

/*==============================================================
  수의사 상담 기능 샘플 데이터
  생성일: 2025-07-22
  설명: 테스트용 샘플 데이터
  
  전제조건:
  - vet_consultation_schema.sql 실행 완료
  - VET 테이블에 vet_id = 1 존재 (user_id = 2)
  - USERS 테이블에 일반 사용자 존재 (user_id = 4)
  - PET 테이블에 반려동물 데이터 존재
==============================================================*/

-- 1. VET_PROFILE 데이터 (수의사 프로필)
INSERT INTO vet_profile (
    vet_id, 
    introduction, 
    consultation_fee,
    is_available,
    is_online,
    rating_avg,
    rating_count,
    consultation_count,
    response_time_avg
) VALUES (
    1, 
    '안녕하세요, 15년 경력의 반려동물 전문 수의사입니다.' || CHR(10) || CHR(10) ||
    '【전문 분야】' || CHR(10) ||
    '• 내과: 소화기, 호흡기, 순환기 질환' || CHR(10) ||
    '• 외과: 중성화 수술, 종양 제거' || CHR(10) ||
    '• 피부과: 아토피, 알레르기 치료' || CHR(10) ||
    '• 노령 동물 케어' || CHR(10) || CHR(10) ||
    '【진료 철학】' || CHR(10) ||
    '반려동물도 가족입니다. 보호자님의 마음으로 최선의 치료를 제공하겠습니다.',
    30000,  -- 30분 기본 상담료
    'Y',    -- 상담 가능
    'N',    -- 현재 오프라인
    4.8,    -- 평균 평점
    125,    -- 평가 횟수
    1250,   -- 총 상담 횟수
    3       -- 평균 응답 시간 3분
);

-- 2. VET_SCHEDULE 데이터 (향후 7일간 스케줄)
DECLARE
    v_date DATE;
    v_vet_id NUMBER := 1;
BEGIN
    FOR i IN 0..6 LOOP
        v_date := TRUNC(SYSDATE) + i;
        
        -- 오전 시간대 (09:00-12:00)
        INSERT INTO vet_schedule (vet_id, schedule_date, start_time, end_time, max_consultations)
        VALUES (v_vet_id, v_date, '09:00', '12:00', 6);
        
        -- 오후 시간대 (14:00-18:00)
        INSERT INTO vet_schedule (vet_id, schedule_date, start_time, end_time, max_consultations)
        VALUES (v_vet_id, v_date, '14:00', '18:00', 8);
        
        -- 저녁 시간대 (19:00-21:00) - 월,수,금만
        IF MOD(i, 2) = 0 THEN
            INSERT INTO vet_schedule (vet_id, schedule_date, start_time, end_time, max_consultations)
            VALUES (v_vet_id, v_date, '19:00', '21:00', 4);
        END IF;
    END LOOP;
END;
/

-- 3. 완료된 상담 샘플 (리뷰 포함)
-- 상담방 1
INSERT INTO consultation_room (
    user_id, vet_id, pet_id, room_status, consultation_type,
    scheduled_datetime, started_at, ended_at, duration_minutes,
    consultation_fee, payment_status, payment_method,
    chief_complaint, consultation_notes, prescription
) VALUES (
    4, 1, 1, 'COMPLETED', 'CHAT',
    SYSDATE - 7, SYSDATE - 7 + (1/24), SYSDATE - 7 + (1.5/24), 30,
    30000, 'PAID', 'CARD',
    '최근 3일간 식욕 부진과 구토 증상이 있습니다.',
    '증상: 식욕부진, 구토 (3일간)' || CHR(10) ||
    '검사 권고: 혈액검사, 복부 초음파' || CHR(10) ||
    '임시 처방: 위장 보호제, 수액 처치',
    '1. 메토클로프라미드 5mg - 하루 2회' || CHR(10) ||
    '2. 수크랄페이트 500mg - 하루 3회' || CHR(10) ||
    '3. 금식 12시간 후 소량씩 급여'
);

-- 상담방 1의 채팅 메시지
INSERT INTO chat_message (room_id, sender_id, sender_type, message_type, content, is_read, read_at)
VALUES (1, 4, 'USER', 'TEXT', '안녕하세요, 선생님. 우리 강아지가 3일째 밥을 잘 안 먹어요.', 'Y', SYSDATE - 7 + (1/24));

INSERT INTO chat_message (room_id, sender_id, sender_type, message_type, content, is_read, read_at)
VALUES (1, 2, 'VET', 'TEXT', '안녕하세요. 걱정이 많으시겠네요. 구토 증상도 있나요?', 'Y', SYSDATE - 7 + (1/24));

INSERT INTO chat_message (room_id, sender_id, sender_type, message_type, content, is_read, read_at)
VALUES (1, 4, 'USER', 'TEXT', '네, 어제 2번 정도 토했어요. 노란색 거품이었어요.', 'Y', SYSDATE - 7 + (1/24));

INSERT INTO chat_message (room_id, sender_id, sender_type, message_type, content, is_read, read_at, is_important)
VALUES (1, 2, 'VET', 'TEXT', 
'공복 시간이 길어져서 나타나는 증상으로 보입니다. 일단 급한 상황은 아니지만, 증상이 지속되면 병원 방문을 권합니다.' || CHR(10) || CHR(10) ||
'【임시 대처법】' || CHR(10) ||
'1. 소량씩 자주 급여 (4-6시간 간격)' || CHR(10) ||
'2. 미지근한 물 제공' || CHR(10) ||
'3. 스트레스 요인 제거', 
'Y', SYSDATE - 7 + (1/24), 'Y');

-- 상담방 1의 리뷰
INSERT INTO consultation_review (
    room_id, user_id, vet_id, rating,
    kindness_score, professional_score, response_score,
    review_content
) VALUES (
    1, 4, 1, 5,
    5, 5, 5,
    '친절하고 자세한 설명 감사합니다. 알려주신 대로 했더니 많이 좋아졌어요!'
);

-- 4. 진행 중인 상담 샘플
INSERT INTO consultation_room (
    user_id, vet_id, pet_id, room_status, consultation_type,
    scheduled_datetime, started_at,
    consultation_fee, payment_status,
    chief_complaint
) VALUES (
    4, 1, 2, 'IN_PROGRESS', 'CHAT',
    SYSDATE, SYSDATE,
    30000, 'PAID',
    '고양이 눈에서 눈물이 계속 나와요.'
);

-- 5. 예약된 상담 샘플
INSERT INTO consultation_room (
    user_id, vet_id, pet_id, schedule_id, room_status, consultation_type,
    scheduled_datetime, consultation_fee, payment_status,
    chief_complaint
) VALUES (
    4, 1, 1, 1, 'CREATED', 'CHAT',
    TRUNC(SYSDATE) + 1 + (14/24), -- 내일 오후 2시
    30000, 'PENDING',
    '정기 건강 검진 상담'
);

-- 통계 업데이트 (트리거가 없는 경우 수동 업데이트)
UPDATE vet_profile 
SET last_online_at = SYSDATE
WHERE vet_id = 1;

COMMIT;


-- 1. 수의사 사용자 계정 생성 (비밀번호: password123)
-- 주의: 실제 비밀번호는 BCrypt로 암호화되어야 함
-- $2a$10$dS0.gUl8gYr6LCJvwY9AOe6kVv0tNNLMAWnrsfUvKUoKFsVvDJUEe = password123

-- 수의사 1
INSERT INTO USERS (LOGIN_ID, USER_PWD, USER_NAME, NICKNAME, USER_EMAIL, PHONE, GENDER, AGE, ADDRESS, ROLE)
VALUES ('vet004', '$2a$10$dS0.gUl8gYr6LCJvwY9AOe6kVv0tNNLMAWnrsfUvKUoKFsVvDJUEe', 
        '김수의', '강아지전문의', 'vet004@duopet.com', '010-1111-2222', 'M', 
        38, '서울시 강남구', 'vet');

-- 수의사 2
INSERT INTO USERS (LOGIN_ID, USER_PWD, USER_NAME, NICKNAME, USER_EMAIL, PHONE, GENDER, AGE, ADDRESS, ROLE)
VALUES ('vet005', '$2a$10$dS0.gUl8gYr6LCJvwY9AOe6kVv0tNNLMAWnrsfUvKUoKFsVvDJUEe', 
        '이수의', '고양이전문의', 'vet005@duopet.com', '010-3333-4444', 'F', 
        33, '서울시 서초구', 'vet');

-- 수의사 3
INSERT INTO USERS (LOGIN_ID, USER_PWD, USER_NAME, NICKNAME, USER_EMAIL, PHONE, GENDER, AGE, ADDRESS, ROLE)
VALUES ('vet006', '$2a$10$dS0.gUl8gYr6LCJvwY9AOe6kVv0tNNLMAWnrsfUvKUoKFsVvDJUEe', 
        '박수의', '특수동물전문의', 'vet006@duopet.com', '010-5555-6666', 'M', 
        35, '서울시 송파구', 'vet');

-- 2. VET 테이블에 수의사 정보 추가
INSERT INTO VET (USER_ID, NAME, LICENSE_NUMBER, PHONE, EMAIL, ADDRESS, SPECIALIZATION)
SELECT USER_ID, '김수의', 'VET2024-' || LPAD(USER_ID, 4, '0'), '010-1111-2222', 'vet004@duopet.com', 
       '서울시 강남구', '강아지 내과, 피부과'
FROM USERS WHERE LOGIN_ID = 'vet004';

INSERT INTO VET (USER_ID, NAME, LICENSE_NUMBER, PHONE, EMAIL, ADDRESS, SPECIALIZATION)
SELECT USER_ID, '이수의', 'VET2024-' || LPAD(USER_ID, 4, '0'), '010-3333-4444', 'vet005@duopet.com',
       '서울시 서초구', '고양이 내과, 행동학'
FROM USERS WHERE LOGIN_ID = 'vet005';

INSERT INTO VET (USER_ID, NAME, LICENSE_NUMBER, PHONE, EMAIL, ADDRESS, SPECIALIZATION)
SELECT USER_ID, '박수의', 'VET2024-' || LPAD(USER_ID, 4, '0'), '010-5555-6666', 'vet006@duopet.com',
       '서울시 송파구', '특수동물, 외과'
FROM USERS WHERE LOGIN_ID = 'vet006';

-- 3. VET_PROFILE 테이블에 수의사 프로필 추가
INSERT INTO VET_PROFILE (VET_ID, INTRODUCTION, CONSULTATION_FEE, IS_AVAILABLE, IS_ONLINE, RATING_AVG, CONSULTATION_COUNT)
SELECT VET_ID, '안녕하세요. 강아지 전문 수의사 김수의입니다. 10년 이상의 임상 경험을 바탕으로 반려견의 건강을 책임지겠습니다.',
       30000, 'Y', 'Y', 4.8, 150
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet004');

INSERT INTO VET_PROFILE (VET_ID, INTRODUCTION, CONSULTATION_FEE, IS_AVAILABLE, IS_ONLINE, RATING_AVG, CONSULTATION_COUNT)
SELECT VET_ID, '고양이 전문 수의사 이수의입니다. 고양이의 특성을 이해하고 맞춤형 진료를 제공합니다.',
       25000, 'Y', 'Y', 4.9, 230
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet005');

INSERT INTO VET_PROFILE (VET_ID, INTRODUCTION, CONSULTATION_FEE, IS_AVAILABLE, IS_ONLINE, RATING_AVG, CONSULTATION_COUNT)
SELECT VET_ID, '토끼, 햄스터, 파충류 등 특수동물 진료 전문입니다. 각 동물의 특성에 맞는 세심한 진료를 약속드립니다.',
       35000, 'Y', 'N', 4.7, 80
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet006');

-- 4. 수의사 일정 추가 (오늘부터 7일간)
-- 수의사 1의 일정 (오전 9-10시)
INSERT INTO VET_SCHEDULE (VET_ID, SCHEDULE_DATE, START_TIME, END_TIME, MAX_CONSULTATIONS, CURRENT_BOOKINGS, IS_AVAILABLE)
SELECT VET_ID, TRUNC(SYSDATE) + LEVEL - 1, '09:00', '10:00', 3, 0, 'Y'
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet004')
CONNECT BY LEVEL <= 7;

-- 수의사 1의 일정 (오후 2-3시)
INSERT INTO VET_SCHEDULE (VET_ID, SCHEDULE_DATE, START_TIME, END_TIME, MAX_CONSULTATIONS, CURRENT_BOOKINGS, IS_AVAILABLE)
SELECT VET_ID, TRUNC(SYSDATE) + LEVEL - 1, '14:00', '15:00', 3, 0, 'Y'
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet004')
CONNECT BY LEVEL <= 7;

-- 수의사 2의 일정 (오전 10-11시)
INSERT INTO VET_SCHEDULE (VET_ID, SCHEDULE_DATE, START_TIME, END_TIME, MAX_CONSULTATIONS, CURRENT_BOOKINGS, IS_AVAILABLE)
SELECT VET_ID, TRUNC(SYSDATE) + LEVEL - 1, '10:00', '11:00', 4, 0, 'Y'
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet005')
CONNECT BY LEVEL <= 7;

-- 수의사 2의 일정 (오후 4-5시)
INSERT INTO VET_SCHEDULE (VET_ID, SCHEDULE_DATE, START_TIME, END_TIME, MAX_CONSULTATIONS, CURRENT_BOOKINGS, IS_AVAILABLE)
SELECT VET_ID, TRUNC(SYSDATE) + LEVEL - 1, '16:00', '17:00', 4, 0, 'Y'
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet005')
CONNECT BY LEVEL <= 7;

-- 수의사 3의 일정 (오전 11시-12시)
INSERT INTO VET_SCHEDULE (VET_ID, SCHEDULE_DATE, START_TIME, END_TIME, MAX_CONSULTATIONS, CURRENT_BOOKINGS, IS_AVAILABLE)
SELECT VET_ID, TRUNC(SYSDATE) + LEVEL - 1, '11:00', '12:00', 2, 0, 'Y'
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet006')
CONNECT BY LEVEL <= 7;

-- 수의사 3의 일정 (오후 3-4시)
INSERT INTO VET_SCHEDULE (VET_ID, SCHEDULE_DATE, START_TIME, END_TIME, MAX_CONSULTATIONS, CURRENT_BOOKINGS, IS_AVAILABLE)
SELECT VET_ID, TRUNC(SYSDATE) + LEVEL - 1, '15:00', '16:00', 2, 0, 'Y'
FROM VET WHERE USER_ID = (SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet006')
CONNECT BY LEVEL <= 7;

-- 커밋
COMMIT;


-- 페이지네이션 테스트를 위한 추가 수의사 데이터 생성 (최종 수정본)

-- 기존 데이터 확인
SELECT COUNT(*) FROM VET_PROFILE;

-- 5번째 수의사 추가
INSERT INTO USERS (LOGIN_ID, USER_PWD, USER_NAME, NICKNAME, PHONE, USER_EMAIL, ROLE, CREATED_AT, STATUS)
VALUES ('vet5', '$2a$10$Jrg0k3f7gXJvN7k9W6T9tOAeGCfKKcnUxzD7RvxrX.MYuP6/pKq36', '박안과', '박안과', '010-5555-5555', 'vet5@duopet.com', 'VET', SYSDATE, 'active');

INSERT INTO VET (USER_ID, NAME, LICENSE_NUMBER, SPECIALIZATION, ADDRESS, EMAIL, ORIGINAL_FILENAME, RENAME_FILENAME)
VALUES ((SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet5'), '박안과', 'VET-2024-005', '안과', '서울시 마포구', 'vet5@duopet.com', 'license5.jpg', 'license5_rename.jpg');

INSERT INTO VET_PROFILE (VET_ID, INTRODUCTION, CONSULTATION_FEE, IS_AVAILABLE, IS_ONLINE, RATING_AVG, RATING_COUNT, CONSULTATION_COUNT, RESPONSE_TIME_AVG, CREATED_AT)
VALUES ((SELECT VET_ID FROM VET WHERE NAME = '박안과'), '안과 질환 전문 진료 15년차입니다.', 35000, 'Y', 'N', 4.8, 120, 350, 12, SYSDATE);

-- 6번째 수의사 추가
INSERT INTO USERS (LOGIN_ID, USER_PWD, USER_NAME, NICKNAME, PHONE, USER_EMAIL, ROLE, CREATED_AT, STATUS)
VALUES ('vet6', '$2a$10$Jrg0k3f7gXJvN7k9W6T9tOAeGCfKKcnUxzD7RvxrX.MYuP6/pKq36', '최치과', '최치과', '010-6666-6666', 'vet6@duopet.com', 'VET', SYSDATE, 'active');

INSERT INTO VET (USER_ID, NAME, LICENSE_NUMBER, SPECIALIZATION, ADDRESS, EMAIL, ORIGINAL_FILENAME, RENAME_FILENAME)
VALUES ((SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet6'), '최치과', 'VET-2024-006', '치과', '서울시 용산구', 'vet6@duopet.com', 'license6.jpg', 'license6_rename.jpg');

INSERT INTO VET_PROFILE (VET_ID, INTRODUCTION, CONSULTATION_FEE, IS_AVAILABLE, IS_ONLINE, RATING_AVG, RATING_COUNT, CONSULTATION_COUNT, RESPONSE_TIME_AVG, CREATED_AT)
VALUES ((SELECT VET_ID FROM VET WHERE NAME = '최치과'), '치과 및 구강 질환 전문입니다.', 40000, 'Y', 'Y', 4.7, 98, 280, 13, SYSDATE);

-- 7번째 수의사 추가
INSERT INTO USERS (LOGIN_ID, USER_PWD, USER_NAME, NICKNAME, PHONE, USER_EMAIL, ROLE, CREATED_AT, STATUS)
VALUES ('vet7', '$2a$10$Jrg0k3f7gXJvN7k9W6T9tOAeGCfKKcnUxzD7RvxrX.MYuP6/pKq36', '윤정형', '윤정형', '010-7777-7777', 'vet7@duopet.com', 'VET', SYSDATE, 'active');

INSERT INTO VET (USER_ID, NAME, LICENSE_NUMBER, SPECIALIZATION, ADDRESS, EMAIL, ORIGINAL_FILENAME, RENAME_FILENAME)
VALUES ((SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet7'), '윤정형', 'VET-2024-007', '정형외과', '서울시 서대문구', 'vet7@duopet.com', 'license7.jpg', 'license7_rename.jpg');

INSERT INTO VET_PROFILE (VET_ID, INTRODUCTION, CONSULTATION_FEE, IS_AVAILABLE, IS_ONLINE, RATING_AVG, RATING_COUNT, CONSULTATION_COUNT, RESPONSE_TIME_AVG, CREATED_AT)
VALUES ((SELECT VET_ID FROM VET WHERE NAME = '윤정형'), '정형외과 및 재활치료 전문입니다.', 50000, 'Y', 'N', 4.9, 150, 420, 11, SYSDATE);

-- 8번째 수의사 추가
INSERT INTO USERS (LOGIN_ID, USER_PWD, USER_NAME, NICKNAME, PHONE, USER_EMAIL, ROLE, CREATED_AT, STATUS)
VALUES ('vet8', '$2a$10$Jrg0k3f7gXJvN7k9W6T9tOAeGCfKKcnUxzD7RvxrX.MYuP6/pKq36', '한영상', '한영상', '010-8888-8888', 'vet8@duopet.com', 'VET', SYSDATE, 'active');

INSERT INTO VET (USER_ID, NAME, LICENSE_NUMBER, SPECIALIZATION, ADDRESS, EMAIL, ORIGINAL_FILENAME, RENAME_FILENAME)
VALUES ((SELECT USER_ID FROM USERS WHERE LOGIN_ID = 'vet8'), '한영상', 'VET-2024-008', '영상의학과', '서울시 은평구', 'vet8@duopet.com', 'license8.jpg', 'license8_rename.jpg');

INSERT INTO VET_PROFILE (VET_ID, INTRODUCTION, CONSULTATION_FEE, IS_AVAILABLE, IS_ONLINE, RATING_AVG, RATING_COUNT, CONSULTATION_COUNT, RESPONSE_TIME_AVG, CREATED_AT)
VALUES ((SELECT VET_ID FROM VET WHERE NAME = '한영상'), 'X-ray, 초음파 판독 전문입니다.', 45000, 'Y', 'Y', 4.6, 80, 220, 15, SYSDATE);

COMMIT;

/*================
2025-07-22
COMMENTS, CONTENT 테이블 STATUS 컬럼 생성
=================*/


ALTER TABLE CONTENT
    ADD (status VARCHAR2(10) DEFAULT 'ACTIVE' NOT NULL);

ALTER TABLE COMMENTS
    ADD (status VARCHAR2(10) DEFAULT 'ACTIVE' NOT NULL);

COMMENT ON COLUMN CONTENT.status IS '상태 (ACTIVE, INACTIVE)';

COMMENT ON COLUMN COMMENTS.status IS '상태 (ACTIVE, INACTIVE)';

/*================
2025-07-24
"LIKE" 테이블 명 LIKE_TB로 변경 
=================*/

ALTER TABLE "LIKE" RENAME TO LIKE_TB;

commit;

/*================
2025-07-25
"COMMENTS" 테이블 CONTENT 컬럼 VARCHAR2(4000 CHAR)으로 변경
=================*/

ALTER TABLE COMMENTS MODIFY (CONTENT VARCHAR2(4000 CHAR));