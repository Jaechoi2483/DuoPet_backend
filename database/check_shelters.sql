-- 보호소 확인
SELECT shelter_id, shelter_name FROM shelter;

-- 보호소가 없다면 테스트용 보호소 추가
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

-- 보호소 재확인
SELECT shelter_id, shelter_name FROM shelter;