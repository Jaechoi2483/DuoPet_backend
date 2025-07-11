-- 샘플 입양 동물 데이터 추가
-- 주의: 실제 공공 API 데이터가 들어오면 중복될 수 있으므로 테스트 후 삭제 권장

-- 강아지 데이터
INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-001', '개', '믹스견', '2년', 'M', 'Y',
    '2025-07-01', '서울특별시 강남구', '갈색 털, 온순한 성격', 'PN-2025-001',
    '2025-07-01', '2025-07-15', 'https://cdn.pixabay.com/photo/2016/02/19/15/46/labrador-retriever-1210559_640.jpg',
    'AVAILABLE', '15kg', '갈색', '보호중', '서울동물보호센터', '02-123-4567',
    '서울특별시 강남구 테헤란로 123', '서울시청', '김보호', '02-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-002', '개', '진돗개', '1년', 'F', 'N',
    '2025-07-03', '서울특별시 송파구', '흰색 털, 활발함', 'PN-2025-002',
    '2025-07-03', '2025-07-17', 'https://cdn.pixabay.com/photo/2019/08/19/07/45/corgi-4415649_640.jpg',
    'AVAILABLE', '12kg', '흰색', '보호중', '서울동물보호센터', '02-123-4567',
    '서울특별시 강남구 테헤란로 123', '서울시청', '김보호', '02-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

-- 고양이 데이터
INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-003', '고양이', '코리안숏헤어', '6개월', 'M', 'N',
    '2025-07-05', '경기도 성남시', '검은색 털, 노란 눈', 'PN-2025-003',
    '2025-07-05', '2025-07-19', 'https://cdn.pixabay.com/photo/2014/11/30/14/11/cat-551554_640.jpg',
    'AVAILABLE', '3kg', '검은색', '보호중', '경기동물보호센터', '031-123-4567',
    '경기도 성남시 분당구 정자로 123', '경기도청', '이보호', '031-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-004', '고양이', '페르시안', '2년', 'F', 'Y',
    '2025-07-07', '인천광역시 남동구', '크림색 장모종', 'PN-2025-004',
    '2025-07-07', '2025-07-21', 'https://cdn.pixabay.com/photo/2017/02/20/18/03/cat-2083492_640.jpg',
    'AVAILABLE', '4kg', '크림색', '보호중', '인천동물보호센터', '032-123-4567',
    '인천광역시 남동구 구월로 123', '인천시청', '박보호', '032-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

-- 추가 강아지 데이터
INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-005', '개', '골든리트리버', '3년', 'M', 'Y',
    '2025-07-08', '서울특별시 마포구', '황금색 털, 친화적', 'PN-2025-005',
    '2025-07-08', '2025-07-22', 'https://cdn.pixabay.com/photo/2016/02/19/15/46/dog-1210559_640.jpg',
    'AVAILABLE', '25kg', '황금색', '보호중', '서울동물보호센터', '02-123-4567',
    '서울특별시 강남구 테헤란로 123', '서울시청', '김보호', '02-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-006', '개', '푸들', '1년', 'F', 'N',
    '2025-07-09', '경기도 고양시', '흰색 곱슬털', 'PN-2025-006',
    '2025-07-09', '2025-07-23', 'https://cdn.pixabay.com/photo/2016/12/13/05/15/puppy-1903313_640.jpg',
    'AVAILABLE', '5kg', '흰색', '보호중', '경기동물보호센터', '031-123-4567',
    '경기도 성남시 분당구 정자로 123', '경기도청', '이보호', '031-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

-- 추가 고양이 데이터
INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-007', '고양이', '샴', '1년', 'M', 'Y',
    '2025-07-10', '부산광역시 해운대구', '크림색 몸통, 갈색 포인트', 'PN-2025-007',
    '2025-07-10', '2025-07-24', 'https://cdn.pixabay.com/photo/2016/01/20/13/05/cat-1151519_640.jpg',
    'AVAILABLE', '3.5kg', '크림&갈색', '보호중', '부산동물보호센터', '051-123-4567',
    '부산광역시 해운대구 해운대로 123', '부산시청', '최보호', '051-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

INSERT INTO adoption_animal (
    animal_id, desertion_no, animal_type, breed, age, gender, neutered,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url, status,
    weight, color_cd, process_state, care_nm, care_tel, care_addr,
    org_nm, charge_nm, officetel, api_source,
    created_at, updated_at
) VALUES (
    adoption_animal_seq.NEXTVAL, 'TEST-008', '고양이', '러시안블루', '2년', 'F', 'Y',
    '2025-07-11', '대구광역시 수성구', '회색 단모종, 녹색 눈', 'PN-2025-008',
    '2025-07-11', '2025-07-25', 'https://cdn.pixabay.com/photo/2017/11/09/21/41/cat-2934720_640.jpg',
    'AVAILABLE', '4.5kg', '회색', '보호중', '대구동물보호센터', '053-123-4567',
    '대구광역시 수성구 동대구로 123', '대구시청', '정보호', '053-111-2222', 'MANUAL',
    SYSTIMESTAMP, SYSTIMESTAMP
);

COMMIT;

-- 데이터 확인
SELECT COUNT(*) FROM adoption_animal WHERE api_source = 'MANUAL';

-- 샘플 데이터 삭제 (필요시)
-- DELETE FROM adoption_animal WHERE api_source = 'MANUAL';