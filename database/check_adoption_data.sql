-- 입양 동물 테이블 데이터 확인
SELECT COUNT(*) AS total_count FROM adoption_animal;

-- 상위 10개 데이터 확인
SELECT * FROM adoption_animal WHERE ROWNUM <= 10;

-- 상태별 카운트 확인
SELECT status, COUNT(*) as count 
FROM adoption_animal 
GROUP BY status;

-- 프로세스 상태별 카운트 확인
SELECT process_state, COUNT(*) as count 
FROM adoption_animal 
GROUP BY process_state;