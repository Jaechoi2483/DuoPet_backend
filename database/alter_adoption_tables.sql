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

-- 보호소 테이블에 공공 API 연동을 위한 컬럼 추가 (필요시)
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