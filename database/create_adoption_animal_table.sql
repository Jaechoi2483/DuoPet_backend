-- adoption_animal 테이블 생성
CREATE TABLE adoption_animal (
    animal_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    desertion_no VARCHAR2(50) NOT NULL UNIQUE,
    animal_type VARCHAR2(20) NOT NULL,
    breed VARCHAR2(100),
    age VARCHAR2(20),
    gender VARCHAR2(10),
    neutered VARCHAR2(10),
    happen_date VARCHAR2(20),
    happen_place VARCHAR2(255),
    special_mark CLOB,
    public_notice_no VARCHAR2(50),
    public_notice_start VARCHAR2(20),
    public_notice_end VARCHAR2(20),
    image_url VARCHAR2(500),
    status VARCHAR2(20) DEFAULT 'AVAILABLE',
    weight VARCHAR2(20),
    color_cd VARCHAR2(50),
    process_state VARCHAR2(20),
    care_nm VARCHAR2(100),
    care_tel VARCHAR2(30),
    care_addr VARCHAR2(255),
    org_nm VARCHAR2(100),
    charge_nm VARCHAR2(50),
    officetel VARCHAR2(30),
    api_source VARCHAR2(20) DEFAULT 'PUBLIC_API',
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    shelter_id NUMBER,
    CONSTRAINT fk_adoption_shelter FOREIGN KEY (shelter_id) REFERENCES shelter(shelter_id)
);

-- 시퀀스 생성 (IDENTITY 대신 사용하는 경우)
CREATE SEQUENCE adoption_animal_seq START WITH 1 INCREMENT BY 1;

-- 인덱스 생성
CREATE INDEX idx_adoption_status ON adoption_animal(status);
CREATE INDEX idx_adoption_type ON adoption_animal(animal_type);
CREATE INDEX idx_adoption_created ON adoption_animal(created_at);

-- 주석 추가
COMMENT ON TABLE adoption_animal IS '입양 가능 동물 정보';
COMMENT ON COLUMN adoption_animal.animal_id IS 'PK, 동물 고유 식별자';
COMMENT ON COLUMN adoption_animal.desertion_no IS '유기번호 (공공API)';
COMMENT ON COLUMN adoption_animal.animal_type IS '동물 종류 (개, 고양이 등)';
COMMENT ON COLUMN adoption_animal.breed IS '품종';
COMMENT ON COLUMN adoption_animal.age IS '나이';
COMMENT ON COLUMN adoption_animal.gender IS '성별 (M/F)';
COMMENT ON COLUMN adoption_animal.neutered IS '중성화 여부 (Y/N/U)';
COMMENT ON COLUMN adoption_animal.happen_date IS '발견일';
COMMENT ON COLUMN adoption_animal.happen_place IS '발견장소';
COMMENT ON COLUMN adoption_animal.special_mark IS '특징';
COMMENT ON COLUMN adoption_animal.public_notice_no IS '공고번호';
COMMENT ON COLUMN adoption_animal.public_notice_start IS '공고시작일';
COMMENT ON COLUMN adoption_animal.public_notice_end IS '공고종료일';
COMMENT ON COLUMN adoption_animal.image_url IS '이미지 URL';
COMMENT ON COLUMN adoption_animal.status IS '상태 (AVAILABLE/ADOPTED/RESERVED)';
COMMENT ON COLUMN adoption_animal.weight IS '체중';
COMMENT ON COLUMN adoption_animal.color_cd IS '색상';
COMMENT ON COLUMN adoption_animal.process_state IS '처리상태';
COMMENT ON COLUMN adoption_animal.care_nm IS '보호소명';
COMMENT ON COLUMN adoption_animal.care_tel IS '보호소 전화번호';
COMMENT ON COLUMN adoption_animal.care_addr IS '보호소 주소';
COMMENT ON COLUMN adoption_animal.org_nm IS '관할기관';
COMMENT ON COLUMN adoption_animal.charge_nm IS '담당자';
COMMENT ON COLUMN adoption_animal.officetel IS '담당자 연락처';
COMMENT ON COLUMN adoption_animal.api_source IS 'API 출처 (PUBLIC_API/MANUAL)';
COMMENT ON COLUMN adoption_animal.created_at IS '생성일시';
COMMENT ON COLUMN adoption_animal.updated_at IS '수정일시';
COMMENT ON COLUMN adoption_animal.shelter_id IS 'FK, 보호소 ID';

COMMIT;