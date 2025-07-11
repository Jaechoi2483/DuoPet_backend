-- 샘플 보호 동물 데이터 추가
-- 주의: 실제 공공 API 데이터가 들어오면 중복될 수 있으므로 테스트 후 삭제 권장

-- 보호소가 없으면 생성
SET DEFINE OFF;
INSERT INTO shelter (shelter_id, shelter_name, phone, address, user_id)
SELECT 1, '서울동물보호센터', '02-123-4567', '서울특별시 강남구 테헤란로 123', 
       (SELECT user_id FROM users WHERE role = 'shelter' AND ROWNUM = 1)
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM shelter WHERE shelter_id = 1);

INSERT INTO shelter (shelter_id, shelter_name, phone, address, user_id)
SELECT 2, '경기동물보호센터', '031-123-4567', '경기도 성남시 분당구 정자로 123', 
       (SELECT user_id FROM users WHERE role = 'shelter' AND ROWNUM = 1)
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM shelter WHERE shelter_id = 2);

COMMIT;

-- 강아지 데이터
INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    1, '루키', '개', '믹스견', 2, 'M', 'Y',
    'AVAILABLE', TO_DATE('2025-07-01', 'YYYY-MM-DD'), '갈색 털, 온순한 성격', 'TEST-001',
    TO_DATE('2025-07-01', 'YYYY-MM-DD'), '서울특별시 강남구', '갈색 털, 온순한 성격', 'PN-2025-001',
    TO_DATE('2025-07-01', 'YYYY-MM-DD'), TO_DATE('2025-07-15', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2016/02/19/15/46/labrador-retriever-1210559_640.jpg',
    'MANUAL', 15.0, '갈색', 'protect'
);

INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    1, '백구', '개', '진돗개', 1, 'F', 'N',
    'AVAILABLE', TO_DATE('2025-07-03', 'YYYY-MM-DD'), '흰색 털, 활발함', 'TEST-002',
    TO_DATE('2025-07-03', 'YYYY-MM-DD'), '서울특별시 송파구', '흰색 털, 활발함', 'PN-2025-002',
    TO_DATE('2025-07-03', 'YYYY-MM-DD'), TO_DATE('2025-07-17', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2019/08/19/07/45/corgi-4415649_640.jpg',
    'MANUAL', 12.0, '흰색', 'protect'
);

-- 고양이 데이터
INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    2, '까미', '고양이', '코리안숏헤어', 0, 'M', 'N',
    'AVAILABLE', TO_DATE('2025-07-05', 'YYYY-MM-DD'), '검은색 털, 노란 눈', 'TEST-003',
    TO_DATE('2025-07-05', 'YYYY-MM-DD'), '경기도 성남시', '검은색 털, 노란 눈', 'PN-2025-003',
    TO_DATE('2025-07-05', 'YYYY-MM-DD'), TO_DATE('2025-07-19', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2014/11/30/14/11/cat-551554_640.jpg',
    'MANUAL', 3.0, '검은색', 'protect'
);

INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    2, '크림이', '고양이', '페르시안', 2, 'F', 'Y',
    'AVAILABLE', TO_DATE('2025-07-07', 'YYYY-MM-DD'), '크림색 장모종', 'TEST-004',
    TO_DATE('2025-07-07', 'YYYY-MM-DD'), '인천광역시 남동구', '크림색 장모종', 'PN-2025-004',
    TO_DATE('2025-07-07', 'YYYY-MM-DD'), TO_DATE('2025-07-21', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2017/02/20/18/03/cat-2083492_640.jpg',
    'MANUAL', 4.0, '크림색', 'protect'
);

-- 추가 강아지 데이터
INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    1, '골디', '개', '골든리트리버', 3, 'M', 'Y',
    'AVAILABLE', TO_DATE('2025-07-08', 'YYYY-MM-DD'), '황금색 털, 친화적', 'TEST-005',
    TO_DATE('2025-07-08', 'YYYY-MM-DD'), '서울특별시 마포구', '황금색 털, 친화적', 'PN-2025-005',
    TO_DATE('2025-07-08', 'YYYY-MM-DD'), TO_DATE('2025-07-22', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2016/02/19/15/46/dog-1210559_640.jpg',
    'MANUAL', 25.0, '황금색', 'protect'
);

INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    2, '콩이', '개', '푸들', 1, 'F', 'N',
    'AVAILABLE', TO_DATE('2025-07-09', 'YYYY-MM-DD'), '흰색 곱슬털', 'TEST-006',
    TO_DATE('2025-07-09', 'YYYY-MM-DD'), '경기도 고양시', '흰색 곱슬털', 'PN-2025-006',
    TO_DATE('2025-07-09', 'YYYY-MM-DD'), TO_DATE('2025-07-23', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2016/12/13/05/15/puppy-1903313_640.jpg',
    'MANUAL', 5.0, '흰색', 'protect'
);

-- 추가 고양이 데이터
INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    1, '시미', '고양이', '샴', 1, 'M', 'Y',
    'AVAILABLE', TO_DATE('2025-07-10', 'YYYY-MM-DD'), '크림색 몸통, 갈색 포인트', 'TEST-007',
    TO_DATE('2025-07-10', 'YYYY-MM-DD'), '부산광역시 해운대구', '크림색 몸통, 갈색 포인트', 'PN-2025-007',
    TO_DATE('2025-07-10', 'YYYY-MM-DD'), TO_DATE('2025-07-24', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2016/01/20/13/05/cat-1151519_640.jpg',
    'MANUAL', 3.5, '크림&갈색', 'protect'
);

INSERT INTO shelter_animals (
    shelter_id, name, animal_type, breed, age, gender, neutered,
    status, intake_date, description, desertion_no,
    happen_date, happen_place, special_mark, public_notice_no,
    public_notice_start, public_notice_end, image_url,
    api_source, weight, color_cd, process_state
) VALUES (
    2, '루나', '고양이', '러시안블루', 2, 'F', 'Y',
    'AVAILABLE', TO_DATE('2025-07-11', 'YYYY-MM-DD'), '회색 단모종, 녹색 눈', 'TEST-008',
    TO_DATE('2025-07-11', 'YYYY-MM-DD'), '대구광역시 수성구', '회색 단모종, 녹색 눈', 'PN-2025-008',
    TO_DATE('2025-07-11', 'YYYY-MM-DD'), TO_DATE('2025-07-25', 'YYYY-MM-DD'), 'https://cdn.pixabay.com/photo/2017/11/09/21/41/cat-2934720_640.jpg',
    'MANUAL', 4.5, '회색', 'protect'
);

COMMIT;

-- 데이터 확인
SELECT COUNT(*) AS total_count FROM shelter_animals WHERE api_source = 'MANUAL';

-- 샘플 데이터 삭제 (필요시)
-- DELETE FROM shelter_animals WHERE api_source = 'MANUAL';