SET DEFINE OFF;
/*==============================================================
  0.  기존 객체 정리 - 순서 무관, CASCADE CONSTRAINTS 사용
==============================================================*/
DROP TABLE bookmark                 CASCADE CONSTRAINTS;
DROP TABLE "LIKE"                   CASCADE CONSTRAINTS;
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
('vet001', 'hashed_password_2', 'NONE', NULL, '김수의', 'drkim', '010-2222-2222',
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


-- 강아지: 코코
INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age,
    gender, neutered, status, intake_date, description, profile_image,
    created_at, updated_at, rename_filename, original_filename
) VALUES 
(1, '코코', '강아지', '푸들', 3, 'F', 'Y', 'AVAILABLE',
 TO_DATE('2024-05-10', 'YYYY-MM-DD'),
 '활발하고 사람을 좋아하는 푸들입니다.',
 NULL, TO_DATE('2024-05-10', 'YYYY-MM-DD'), TO_DATE('2024-06-01', 'YYYY-MM-DD'),
 NULL, NULL);

-- 고양이: 나비
INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age,
    gender, neutered, status, intake_date, description, profile_image,
    created_at, updated_at, rename_filename, original_filename
) VALUES 
(1, '나비', '고양이', '코리안숏헤어', 2, 'M', 'N', 'AVAILABLE',
 TO_DATE('2024-05-15', 'YYYY-MM-DD'),
 '온순하고 조용한 성격의 고양이입니다.',
 NULL, TO_DATE('2024-05-15', 'YYYY-MM-DD'), TO_DATE('2024-06-01', 'YYYY-MM-DD'),
 NULL, NULL);


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

COMMENT ON TABLE pet                              IS '회원 등록 반려동물 정보';
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

COMMENT ON TABLE pet_vaccin                       IS '반려동물 예방 접종 기록';
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

COMMENT ON TABLE pet_weight                   IS '반려동물 체중 기록';
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


CREATE TABLE "LIKE" (
    like_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER        NOT NULL,
    target_id   NUMBER        NOT NULL,
    target_type VARCHAR2(50)  NOT NULL,
    created_at  DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT ck_like_type CHECK (target_type IN ('board','comment','info_board')),
    CONSTRAINT uq_like_once UNIQUE (user_id, target_id, target_type),
    CONSTRAINT fk_like_user  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

COMMENT ON TABLE "LIKE"                     IS '좋아요 기록';
COMMENT ON COLUMN "LIKE".like_id          IS 'PK, IDENTITY (좋아요 고유 식별자)';
COMMENT ON COLUMN "LIKE".user_id          IS 'FK (USERS 테이블의 user_id 참조), 좋아요를 누른 사용자 고유 식별자';
COMMENT ON COLUMN "LIKE".target_id        IS '좋아요 대상의 ID (게시글 ID, 댓글 ID 등)';
COMMENT ON COLUMN "LIKE".target_type      IS '좋아요 대상의 유형 (board: 게시글, comment: 댓글, info_board: 정보 게시판)';
COMMENT ON COLUMN "LIKE".created_at       IS '좋아요 누른 시간';

INSERT INTO "LIKE" (
    user_id, target_id, target_type, created_at
) VALUES
(4, 20, 'board', SYSDATE);

INSERT INTO "LIKE" (
    user_id, target_id, target_type, created_at
) VALUES
(2, 14, 'board',   SYSDATE);

INSERT INTO "LIKE" (
    user_id, target_id, target_type, created_at
) VALUES
(3, 15, 'board',   SYSDATE);

INSERT INTO "LIKE" (
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
CHECK (status IN ('active', 'inactive', 'suspended', 'waiting', 'rejected'));

COMMENT ON COLUMN users.status IS '계정 상태 (active: 활성, inactive: 비활성, suspended: 정지, waiting: 전문가/보호소 승인 대기,rejected: 전문가/보호소 승인 거절)';